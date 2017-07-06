/* 
 * Kodkod -- Copyright (c) 2005-present, Emina Torlak
 * Pardinus -- Copyright (c) 2013-present, Nuno Macedo, INESC TEC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package kodkod.engine;

import java.util.Iterator;

import kodkod.ast.Formula;
import kodkod.engine.config.TemporalOptions;
import kodkod.instance.TemporalBounds;

/**
 * The interface that should be implemented by model finders that expect to
 * solve temporal problems, i.e., formulas with
 * {@link kodkod.ast.operator.TemporalOperator temporal operators} and bounds
 * over {@link kodkod.ast.VarRelation variable relations}.
 * 
 * @author Nuno Macedo // [HASLab] model finding hierarchy
 *
 * @param <O>
 *            the options containing
 *            {@link kodkod.engine.config.TemporalOptions temporal options}.
 */
public interface TemporalSolver<O extends TemporalOptions<?>> extends PardinusSolver<TemporalBounds, O> {

	@Override
	public Solution solve(Formula formula, TemporalBounds bounds);

	public Iterator<Solution> solveAll(Formula formula, TemporalBounds bounds);

}
