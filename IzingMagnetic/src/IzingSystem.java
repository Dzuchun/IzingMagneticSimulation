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
	V size;
	// Table that contains system state
	public T stateTable;
	// Function that construct new vectors
	Function<ArrayList<Integer>, V> positionConstructor;
	// Function that should return system's temperature
	Supplier<Double> temperature;
	// Function that should return external magnetic field tension
	Supplier<Double> magneticTension;
	Random random;

	@SuppressWarnings("unchecked")
	public IzingSystem(V sizeIn, Function<V, Boolean> spinGeneratorIn, Function<V, T> tableConstructor,
			Function<ArrayList<Integer>, V> positionConstructorIn, Supplier<Double> temperatureIn,
			Supplier<Double> magneticTensionIn) {
		this.size = (V) sizeIn.createClone();
		stateTable = tableConstructor.apply(size);
		stateTable.fill(spinGeneratorIn);
		this.positionConstructor = positionConstructorIn;
		this.temperature = temperatureIn;
		this.magneticTension = magneticTensionIn;
		random = new Random();
	}

	@SuppressWarnings("unchecked")
	private IzingSystem(V sizeIn, T grid, Function<ArrayList<Integer>, V> positionConstructorIn,
			Supplier<Double> temperatureIn, Supplier<Double> magneticTensionIn) {
		this.size = (V) sizeIn.createClone();
		this.stateTable = (T) grid.clone();
		this.positionConstructor = positionConstructorIn;
		this.temperature = temperatureIn;
		this.magneticTension = magneticTensionIn;
		random = new Random();
	}

	@Override
	public ISimulatable<Integer> createCopy() {
		return new IzingSystem<V, T>(this.size, this.stateTable, positionConstructor, temperature, magneticTension);
	}

	private int lastAdvanceFlips;

	public int getLastAdanceFlips() {
		return this.lastAdvanceFlips;
	}

	// Advances @value reaction steps
	@Override
	public void advance(Integer value) {
		lastAdvanceFlips = 0;
		for (int i = 0; i < value; i++) {
			performIzingReaction();
		}
	}

	// Performs one Izing model reaction
	private void performIzingReaction() {
		V target = getRandomPos();
		double E = getEnergyFor(target);
		if (E > 0) {
			this.flipSpin(target);
		} else {
			double r = random.nextDouble();
			double T = temperature.get();
			if (r < Math.exp((2 * E) / T)) {
				this.flipSpin(target);
			}
		}
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

	@SuppressWarnings("unchecked")
	private boolean getCellState(V pos) {
		return this.stateTable
				.get((V) pos.forEachElement((c1, c2, e) -> MathUtil.positiveFloor(e, size.getCoord(c2)), true));
	}

	private int getCellSpin(V pos) {
		return getCellState(pos) ? 1 : -1;
	}

	private void flipSpin(V pos) {
		this.stateTable.assign(pos, !this.stateTable.get(pos));
		lastAdvanceFlips++;
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
		return ((2.0d * this.stateTable.getPositiveSpins()) / this.stateTable.volume()) - 1.0d;
	}

	public double getEnergy() {
		return this.stateTable.calculateParameter(this::getEnergyFor);
	}

}
