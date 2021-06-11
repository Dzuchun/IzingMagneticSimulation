import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.WindowConstants;

import dzuchun.lib.graph.AutopaintingField;
import dzuchun.lib.io.DoubleResult;
import dzuchun.lib.io.SpreadsheetHelper;
import dzuchun.lib.io.SpreadsheetHelper.Result;
import dzuchun.lib.math.DescreteVector2D;
import dzuchun.lib.sim.ISimulatable.Checker;
import dzuchun.lib.sim.Simulator;

public class IzingSimulation {
	private static final long ITERATIONS_PER_STEP = 500000;
	private static long steps = 0;
	private static final long MAX_ITERARIONS = 100000000L;
	private static final long MAX_STEPS = IzingSimulation.MAX_ITERARIONS / IzingSimulation.ITERATIONS_PER_STEP;
	private static final String[] PARAMETER_NAMES = { "Iterations", "Temperature", "Magnetic Tension", "Magnetization",
			"Total Energy", "Flips" };

	public static void main(String[] args) {
		final DescreteVector2D SIZE = new DescreteVector2D(500, 500);
		final double TEMPERATURE = 1.0d;
		final double MAGNETIC_TENSION = 0.05d;
		final long REPAINT_TIME = 30;
		final Random random = new Random();
		IzingSystem<DescreteVector2D, SpinTable2D> system = new IzingSystem<DescreteVector2D, SpinTable2D>(SIZE,
				pos -> random.nextBoolean(), SpinTable2D::new, list -> new DescreteVector2D(list.get(0), list.get(1)),
				() -> TEMPERATURE, () -> MAGNETIC_TENSION);
		Checker<IzingSystem<DescreteVector2D, SpinTable2D>, Integer> checker = new Checker<IzingSystem<DescreteVector2D, SpinTable2D>, Integer>() {

			@Override
			public Integer check(IzingSystem<DescreteVector2D, SpinTable2D> base,
					IzingSystem<DescreteVector2D, SpinTable2D> candidate, Integer advanceParameter)
					throws CannotProceedException {
				// TODO Auto-generated method stub
				return null;
			}
		};
		final Result result = new Result();
		final Simulator<IzingSystem<DescreteVector2D, SpinTable2D>, Integer, Checker<IzingSystem<DescreteVector2D, SpinTable2D>, Integer>> simulator = new Simulator<IzingSystem<DescreteVector2D, SpinTable2D>, Integer, Checker<IzingSystem<DescreteVector2D, SpinTable2D>, Integer>>(
				system, (int) IzingSimulation.ITERATIONS_PER_STEP, checker, s -> {
					if (IzingSimulation.steps < IzingSimulation.MAX_STEPS) {
						return true;
					} else {
						System.out.println(String.format(
								"Simulation finished with parameters: T=%.2f, H=%.3f, D=%.3f, E=%.3f", TEMPERATURE,
								MAGNETIC_TENSION, s.getSimulation().getMagnetization(), s.getSimulation().getEnergy()));
						SpreadsheetHelper.saveResToFile(result, "./saves/data.xlsx");
						return false;
					}
				}, s -> {
					IzingSimulation.steps++;
					@SuppressWarnings("rawtypes")
					IzingSystem state = s.getSimulation();
					result.append(IzingSimulation.PARAMETER_NAMES[0],
							new DoubleResult((double) (IzingSimulation.steps * IzingSimulation.ITERATIONS_PER_STEP)));
					result.append(IzingSimulation.PARAMETER_NAMES[1], new DoubleResult(TEMPERATURE));
					result.append(IzingSimulation.PARAMETER_NAMES[2], new DoubleResult(MAGNETIC_TENSION));
					result.append(IzingSimulation.PARAMETER_NAMES[3], new DoubleResult(state.getMagnetization()));
					result.append(IzingSimulation.PARAMETER_NAMES[4], new DoubleResult(state.getEnergy()));
					result.append(IzingSimulation.PARAMETER_NAMES[5],
							new DoubleResult((double) state.getLastAdanceFlips()));
				});
		AutopaintingField frame = new AutopaintingField(
				() -> IzingSimulation.transformToImage(simulator.getSimulation().getSystemSpinstate()), REPAINT_TIME);
		frame.setSize(new Dimension(900, 700));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		simulator.start();
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
