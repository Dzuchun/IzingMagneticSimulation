import java.util.Collection;
import java.util.function.Function;

import dzuchun.lib.math.DescreteVector;

/**
 * This class is designed to encapsulate a multidimentional array
 *
 * @author dzu
 *
 * @param <V>
 */
public abstract class SpinTable<V extends DescreteVector> {

	public SpinTable(V size) {
	}

	/**
	 * @param pos   location
	 * @param state state to assign
	 */
	public abstract void assign(V pos, boolean state);

	/**
	 * @param pos location
	 * @return stored spin
	 */
	public abstract boolean get(V pos);

	/**
	 * @return collection of neighbour's positions
	 */
	public abstract Collection<V> getNeightbours(V pos);

	/**
	 * @param generator function to determine spin
	 */
	public abstract void fill(Function<V, Boolean> generator);

	/**
	 * Should return new spinTable but with same contents
	 */
	@Override
	public abstract SpinTable<V> clone();

	/*
	 * Should return number of positive spins
	 */
	public abstract int getPositiveSpins();

	/*
	 * Should return entire capacity of the table
	 */
	public abstract int volume();

	/**
	 * This method should evaluate a parameter the getter specified
	 *
	 * @return calculated parameter
	 */
	public abstract double calculateParameter(Function<V, Double> parameterGetter);
}
