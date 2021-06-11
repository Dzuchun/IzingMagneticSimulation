import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import dzuchun.lib.math.DescreteVector2D;
import dzuchun.lib.math.MathUtil;

public class SpinTable2D extends SpinTable<DescreteVector2D> {

	public final DescreteVector2D size;
	private boolean[][] data;
	private final int volume;

	public SpinTable2D(DescreteVector2D sizeIn) {
		super(sizeIn);
		this.size = sizeIn;
		this.data = new boolean[sizeIn.getX()][sizeIn.getY()];
		this.volume = this.size.getX() * this.size.getY();
	}

	@Override
	public void assign(DescreteVector2D pos, boolean state) {
		this.data[pos.getX()][pos.getY()] = state;
	}

	@Override
	public boolean get(DescreteVector2D pos) {
		return this.get(pos.getX(), pos.getY());
	}

	public boolean get(int x, int y) {
		return this.data[MathUtil.positiveFloor(x, this.size.getX())][MathUtil.positiveFloor(y, this.size.getY())];
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
		for (int i = 0; i < this.size.getX(); i++) {
			for (int j = 0; j < this.size.getY(); j++) {
				this.data[i][j] = generator.apply(new DescreteVector2D(i, j));
			}
		}
	}

	@Override
	public SpinTable<DescreteVector2D> clone() {
		SpinTable2D res = new SpinTable2D(size);
		res.data = this.data.clone();
		return res;
	}

	@Override
	public int getPositiveSpins() {
		int res = 0;
		for (int i = 0; i < size.getX(); i++) {
			for (int j = 0; j < size.getY(); j++) {
				if (this.data[i][j]) {
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
		for (int i = 0; i < this.size.getX(); i++) {
			for (int j = 0; j < this.size.getY(); j++) {
				res += parameterGetter.apply(new DescreteVector2D(i, j));
			}
		}
		return res;
	}

}
