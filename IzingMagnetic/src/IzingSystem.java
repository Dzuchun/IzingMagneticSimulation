import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import dzuchun.lib.math.DescreteVector;
import dzuchun.lib.math.MathUtil;
import dzuchun.lib.sim.ISimulatable;

public class IzingSystem<V extends DescreteVector, T extends SpinTable<V>> implements ISimulatable<Integer> {

	// Contains size of a grid
	public final V size;
	// Table that contains system state
	private T stateTable;
	// Function that construct new vectors
	private Function<ArrayList<Integer>, V> positionConstructor;
	// Function that should return system's temperature
	private Supplier<Double> temperature;
	// Function that should return external magnetic field tension
	private Supplier<Double> magneticTension;
	// Interval to perform microsaves in
	private long microsaves;

	private Random random;
	public int positiveSpins;
	public double totalEnergy;

	@SuppressWarnings("unchecked")
	public IzingSystem(V sizeIn, Function<V, Boolean> spinGeneratorIn, Function<V, T> tableConstructor,
			Function<ArrayList<Integer>, V> positionConstructorIn, Supplier<Double> temperatureIn,
			Supplier<Double> magneticTensionIn, long microsavesIn) {
		this.size = (V) sizeIn.createClone();
		stateTable = tableConstructor.apply(size);
		stateTable.fill(spinGeneratorIn);
		this.positionConstructor = positionConstructorIn;
		this.temperature = temperatureIn;
		this.magneticTension = magneticTensionIn;
		this.microsaves = microsavesIn;
		random = new Random();
		this.totalEnergy = this.stateTable.calculateParameter(this::getEnergyFor);
		this.positiveSpins = this.stateTable.getPositiveSpins();
	}

	@SuppressWarnings("unchecked")
	private IzingSystem(V sizeIn, T grid, Function<ArrayList<Integer>, V> positionConstructorIn,
			Supplier<Double> temperatureIn, Supplier<Double> magneticTensionIn, long microsavesIn, double totalEnergyIn,
			int positiveSpinsIn) {
		this.size = (V) sizeIn.createClone();
		this.stateTable = (T) grid.clone();
		this.positionConstructor = positionConstructorIn;
		this.temperature = temperatureIn;
		this.magneticTension = magneticTensionIn;
		this.microsaves = microsavesIn;
		random = new Random();
		this.totalEnergy = totalEnergyIn;
		this.positiveSpins = positiveSpinsIn;
	}

	@Override
	public ISimulatable<Integer> createCopy() {
		return new IzingSystem<V, T>(this.size, this.stateTable, positionConstructor, temperature, magneticTension,
				microsaves, this.totalEnergy, this.positiveSpins);
	}

	private int lastAdvanceFlips;

	public int getLastAdvanceFlips() {
		return this.lastAdvanceFlips;
	}

	public final ArrayList<Double> energies = new ArrayList<Double>(0);
	public final ArrayList<Double> magnetizations = new ArrayList<Double>(0);

	// Advances @value reaction steps
	@Override
	public void advance(Integer value) {
		lastAdvanceFlips = 0;
		energies.clear();
		magnetizations.clear();
		long microsaveInterval = value / microsaves;
		for (int i = 0; i < value; i++) {
			performIzingReaction();
			if ((i % microsaveInterval) == 0) {
				this.energies.add(this.getEnergy());
				this.magnetizations.add(this.getMagnetization());
			}
		}
	}

	// Performs one Izing model reaction
	private void performIzingReaction() {
		V target = getRandomPos();
		double E = getEnergyFor(target);
		if (E > 0) {
			this.flipSpin(target, E);
		} else {
			double r = random.nextDouble();
			double T = temperature.get();
			if (r < Math.exp((2 * E) / T)) {
				this.flipSpin(target, E);
			}
		}
	}

	private void flipSpin(V pos, double energy) {
		boolean state = this.stateTable.get(pos);
		this.stateTable.assign(pos, !state);
		lastAdvanceFlips++;
		totalEnergy += energyDeltaForFlip(state, energy);
		positiveSpins += state ? -1 : 1;
	}

	private double getEnergyFor(V pos) {
		Collection<V> neighbours = stateTable.getNeightbours(pos);
		double E = 0;
		for (V n : neighbours) {
			E += this.getCellSpin(n);
		}
		E += this.magneticTension.get();
		E *= -this.getCellSpin(pos);
		return E;
	}

	private double energyDeltaForFlip(boolean state, double energy) {
		return -2 * energy;
	}

	@SuppressWarnings("unchecked")
	private boolean getCellState(V pos) {
		return this.stateTable
				.get((V) pos.forEachElement((c1, c2, e) -> MathUtil.positiveFloor(e, size.getCoord(c2)), true));
	}

	private int getCellSpin(V pos) {
		return getCellState(pos) ? 1 : -1;
	}

	private V getRandomPos() {
		final ArrayList<Integer> coords = new ArrayList<Integer>(0);
		this.size.forEachCoord((n, c) -> {
			coords.add(random.nextInt(c));
		});
		return positionConstructor.apply(coords);
	}

	@SuppressWarnings("unchecked")
	public T getSystemSpinstate() {
		return (T) this.stateTable.clone();
	}

	public double getMagnetization() {
		return ((2.0d * this.positiveSpins) / this.stateTable.volume()) - 1.0d;
	}

	public double getEnergy() {
		return this.totalEnergy / this.stateTable.volume();
	}

	public int getVolume() {
		return this.stateTable.volume();
	}

}
