import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import dzuchun.lib.graph.AutopaintingField;
import dzuchun.lib.io.DoubleResult;
import dzuchun.lib.io.PrecisedDoubleResult;
import dzuchun.lib.io.SpreadsheetHelper;
import dzuchun.lib.io.SpreadsheetHelper.Result;
import dzuchun.lib.math.DescreteVector2D;
import dzuchun.lib.sim.ISimulatable.Checker;
import dzuchun.lib.sim.Simulator;

public class IzingSimulation {
	private static final long MAX_ITERARIONS = Settings.GRID_SIZE * Settings.GRID_SIZE * Settings.ITERATIONS_DENSITY;
	private static final long ITERATIONS_PER_STEP = IzingSimulation.MAX_ITERARIONS / Settings.MACROSAVES;
	private static final long MAX_STEPS = IzingSimulation.MAX_ITERARIONS / IzingSimulation.ITERATIONS_PER_STEP;
	private static final String[] PARAMETER_NAMES = { "Iterations Density", "Temperature", "Magnetic Tension",
			"Magnetization", "Total Energy", "Flips per iteration" };
	private final static long REPAINT_TIME = 1000 / Settings.FPS;
	private static final Function<Integer, Double> TEMPERATURE = n -> Settings.TEMPERATURE_0
			+ (Settings.TEMPERATURE_D * n);
	private static final Function<Integer, Double> MAGNETIC_TENSION = n -> Settings.TENSION_0
			+ (Settings.TENSION_D * n);

	public static void main(String[] args) throws InterruptedException {
		try {
			new File(Settings.SAVES_FORDER + Settings.FILENAME_FORMAT).createNewFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Filesave location is invalid!");
			return;
		}
		final DescreteVector2D SIZE = new DescreteVector2D(Settings.GRID_SIZE, Settings.GRID_SIZE);
		final long checkInterval = 1000L;
		final ArrayList<Supplier<Boolean>> runningThreads = new ArrayList<Supplier<Boolean>>(0);
		for (int i = 0; i < Settings.SIMULATIONS; i++) {
			if (runningThreads.size() >= Settings.MAX_THREADS) {
				do {
					for (int j = 0; j < runningThreads.size(); j++) {
						Supplier<Boolean> tmp = runningThreads.get(j);
						if (!tmp.get()) {
							runningThreads.remove(tmp);
						}
					}
					Thread.sleep(checkInterval);
				} while (runningThreads.size() >= Settings.MAX_THREADS);

			}
			runningThreads.add(IzingSimulation.performSimulation(i, SIZE, IzingSimulation.TEMPERATURE.apply(i),
					IzingSimulation.MAGNETIC_TENSION.apply(i), Settings.ENABLE_VISUALIZATION));
			System.gc();
//			Thread.sleep(checkInterval);
		}
		System.out.println("Finished batch");
	}

	private static Supplier<Boolean> performSimulation(int id, DescreteVector2D size, double temperature,
			double magneticTention, boolean enableGui) {
		final Result result = new Result();
		final Random random = new Random();
		IzingSystem<DescreteVector2D, SpinTable2D> system = new IzingSystem<DescreteVector2D, SpinTable2D>(size,
				pos -> random.nextBoolean(), SpinTable2D::new, list -> new DescreteVector2D(list.get(0), list.get(1)),
				() -> temperature, () -> magneticTention, Settings.MICROSAVES);
		Checker<IzingSystem<DescreteVector2D, SpinTable2D>, Integer> checker = new Checker<IzingSystem<DescreteVector2D, SpinTable2D>, Integer>() {
			@Override
			public Integer check(IzingSystem<DescreteVector2D, SpinTable2D> base,
					IzingSystem<DescreteVector2D, SpinTable2D> candidate, Integer advanceParameter)
					throws CannotProceedException {
				return null;
			}
		};
		final int[] steps = { 0 };
		AutopaintingField frame;
		Simulator<IzingSystem<DescreteVector2D, SpinTable2D>, Integer, Checker<IzingSystem<DescreteVector2D, SpinTable2D>, Integer>> simulator = new Simulator<IzingSystem<DescreteVector2D, SpinTable2D>, Integer, Checker<IzingSystem<DescreteVector2D, SpinTable2D>, Integer>>(
				system, (int) IzingSimulation.ITERATIONS_PER_STEP, checker, s -> {
					if (steps[0] < IzingSimulation.MAX_STEPS) {
						System.out.println(
								String.format("%d finished step %d/%d", id, steps[0], IzingSimulation.MAX_STEPS));
						return true;
					} else {
						System.out.println(String.format(
								"Simulation finished with parameters: T=%.2f, H=%.3f, D=%.3f, E=%.3f", temperature,
								magneticTention, s.getSimulation().getMagnetization(), s.getSimulation().getEnergy()));
						try {
							SpreadsheetHelper.saveResToFile(result, Settings.SAVES_FORDER + Settings.FILENAME_FORMAT);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						s.endSimulation();
						return false;
					}
				}, s -> {
					steps[0]++;
					@SuppressWarnings("rawtypes")
					IzingSystem state = s.getSimulation();
					result.append(IzingSimulation.PARAMETER_NAMES[0], new DoubleResult(
							((double) steps[0] * IzingSimulation.ITERATIONS_PER_STEP) / state.getVolume()));
					result.append(IzingSimulation.PARAMETER_NAMES[1], new DoubleResult(temperature));
					result.append(IzingSimulation.PARAMETER_NAMES[2], new DoubleResult(magneticTention));
					@SuppressWarnings("unchecked")
					PrecisedDoubleResult magnetization = new PrecisedDoubleResult(state.magnetizations);
					result.append(IzingSimulation.PARAMETER_NAMES[3], magnetization);
					@SuppressWarnings("unchecked")
					PrecisedDoubleResult energy = new PrecisedDoubleResult(state.energies);
					result.append(IzingSimulation.PARAMETER_NAMES[4], energy);
					result.append(IzingSimulation.PARAMETER_NAMES[5], new DoubleResult(
							(double) state.getLastAdvanceFlips() / IzingSimulation.ITERATIONS_PER_STEP));
				});
		final Supplier<Boolean> isSimulationEnded = () -> !simulator.isStopped() && simulator.isAlive();
		if (enableGui) {
			frame = new AutopaintingField(
					() -> IzingSimulation.transformToImage(simulator.getSimulation().getSystemSpinstate()),
					IzingSimulation.REPAINT_TIME, isSimulationEnded);
			frame.setSize(new Dimension(900, 700));
			frame.setTitle("Izing Model");
			frame.setVisible(true);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		}
		simulator.start();
		return isSimulationEnded;
	}

	private static final int SPIN_MINUS = Color.blue.getRGB();
	private static final int SPIN_PLUS = Color.yellow.getRGB();

	private static Image transformToImage(SpinTable2D data) {
		DescreteVector2D size = data.size;
		BufferedImage res = new BufferedImage(size.getX(), size.getY(), BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < size.getX(); i++) {
			for (int j = 0; j < size.getY(); j++) {
				res.setRGB(i, j, data.get(i, j) ? IzingSimulation.SPIN_PLUS : IzingSimulation.SPIN_MINUS);
			}
		}
		return res;
	}
}
