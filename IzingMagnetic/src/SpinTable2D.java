import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import dzuchun.lib.math.DescreteVector2D;
import dzuchun.lib.math.MathUtil;

public class SpinTable2D extends SpinTable<DescreteVector2D> {

	public final DescreteVector2D size;
	private boolean[][] data;
	private final int volume;

	public SpinTable2D(DescreteVector2D sizeIn) {
		super(sizeIn);
		size = sizeIn;
		data = new boolean[sizeIn.getX()][sizeIn.getY()];
		volume = size.getX() * size.getY();
	}

	@Override
	public void assign(DescreteVector2D pos, boolean state) {
		data[pos.getX()][pos.getY()] = state;
	}

	@Override
	public boolean get(DescreteVector2D pos) {
		return this.get(pos.getX(), pos.getY());
	}

	public boolean get(int x, int y) {
		return data[MathUtil.positiveFloor(x, size.getX())][MathUtil.positiveFloor(y, size.getY())];
	}

	@Override
	public Collection<DescreteVector2D> getNeightbours(DescreteVector2D pos) {
		ArrayList<DescreteVector2D> res = new ArrayList<DescreteVector2D>(0);
		res.add((DescreteVector2D) pos.add(new DescreteVector2D(1, 0), true));
		res.add((DescreteVector2D) pos.add(new DescreteVector2D(-1, 0), true));
		res.add((DescreteVector2D) pos.add(new DescreteVector2D(0, 1), true));
		res.add((DescreteVector2D) pos.add(new DescreteVector2D(0, -1), true));
		return res;
	}

	@Override
	public void fill(Function<DescreteVector2D, Boolean> generator) {
		for (int i = 0; i < size.getX(); i++) {
			for (int j = 0; j < size.getY(); j++) {
				data[i][j] = generator.apply(new DescreteVector2D(i, j));
			}
		}
	}

	@Override
	public SpinTable<DescreteVector2D> clone() {
		SpinTable2D res = new SpinTable2D(size);
		res.data = data.clone();
		return res;
	}

	@Override
	public int getPositiveSpins() {
		int res = 0;
		for (int i = 0; i < size.getX(); i++) {
			for (int j = 0; j < size.getY(); j++) {
				if (data[i][j]) {
					res++;
				}
			}
		}
		return res;
	}

	@Override
	public int volume() {
		return volume;
	}

	@Override
	public double calculateParameter(Function<DescreteVector2D, Double> parameterGetter) {
		double res = 0;
		for (int i = 0; i < size.getX(); i++) {
			for (int j = 0; j < size.getY(); j++) {
				res += parameterGetter.apply(new DescreteVector2D(i, j));
			}
		}
		return res;
	}

}
