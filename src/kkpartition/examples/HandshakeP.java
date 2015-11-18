package kkpartition.examples;

import java.util.ArrayList;
import java.util.List;

import kkpartition.PartitionModel;
import kodkod.ast.Expression;
import kodkod.ast.Formula;
import kodkod.ast.IntConstant;
import kodkod.ast.IntExpression;
import kodkod.ast.Relation;
import kodkod.ast.Variable;
import kodkod.instance.Bounds;
import kodkod.instance.TupleFactory;
import kodkod.instance.TupleSet;
import kodkod.instance.Universe;

public class HandshakeP implements PartitionModel {

	final private Relation hypo;
	final private Relation Person, Hilary, Jocelyn, shaken, spouse;
	final private Universe u;
	final private int persons;
	final private boolean var, counter;

	public HandshakeP(Object[] args) {
		Person = Relation.unary("Person");
		Hilary = Relation.unary("Hilary");
		Jocelyn = Relation.unary("Jocelyn");
		shaken = Relation.binary("shaken");
		spouse = Relation.binary("spouse");

		hypo = Relation.unary("hypothesis");
		
		var = (boolean) args[0];
		counter = (boolean) args[1];
		persons = (int) args[2];

		final List<Object> atoms = new ArrayList<Object>((!counter && var)?2*persons-1:persons);
		atoms.add("Hilary");
		atoms.add("Jocelyn");
		for (int i = 3; i <= persons; i++) {
			atoms.add("Person" + i);
		}
		
		// if proving theorem with variable persons, integers must be added to the universe
		if(!counter)
			if (var)
				for (int i = 0; i <= maxInt(); i++)
					atoms.add(Integer.valueOf(i));
			else
				atoms.add(hypo());
		
		u = new Universe(atoms);
	}


	/**
	 * Returns the declarations
	 * 
	 * @return <pre>
	 * sig Person {spouse: Person }
	 * one sig Jocelyn, Hilary extends Person {}
	 * 
	 * fact Spouses {
	 *  all disj p, q: Person {
	 *   // if q is p's spouse, p is q's spouse
	 *   p.spouse = q => q.spouse = p
	 *   // no spouse sharing
	 *  p.spouse != q.spouse
	 *  }
	 *  all p: Person {
	 * 	 // a person is his or her spouse's spouse
	 *  p.spouse.spouse = p
	 *  // nobody is his or her own spouse
	 *  p != p.spouse
	 * }
	 * }
	 * 
	 * pred Puzzle() {
	 *  // Hilary's spouse is Jocelyn
	 *  Hilary.spouse = Jocelyn
	 * }
	 * </pre>
	 */
	public Formula partition1() {
		final Formula f10 = spouse.function(Person, Person);
		final Formula f12 = Hilary.one().and(Jocelyn.one());

		final Variable p = Variable.unary("p");
		final Variable q = Variable.unary("q");
		final Formula f1 = p.join(spouse).eq(q).implies(q.join(spouse).eq(p));
		final Formula f2 = p.join(spouse).eq(q.join(spouse)).not();
		final Formula f3 = p.intersection(q).no().implies(f1.and(f2)).forAll(p.oneOf(Person).and(q.oneOf(Person)));
		final Formula f4 = p.join(spouse).join(spouse).eq(p).and(p.eq(p.join(spouse)).not()).forAll(p.oneOf(Person));
		final Formula f5 = Hilary.join(spouse).eq(Jocelyn);

		Formula res = f10.and(f12).and(f3).and(f4).and(f5);

		// if trying to prove theorem, define the integer value of the hypothesis
		// if variable, value must be defined at runtime; otherwise it can be calculated statically
		if (!counter) {
			final IntExpression nn;
			if(var) 
				nn = ((Person.difference(Hilary)).difference(Jocelyn)).count().divide(IntConstant.constant(2));
			else 
				nn = IntConstant.constant(hypo());
			res = res.and(hypo.eq(nn.toExpression()));
		}
		
		return res;
	}

	/**
	 * Returns the ShakingProtocol fact.
	 * 
	 * @return <pre>
	 * sig Person { shaken: set Person}
	 * 
	 * fact ShakingProtocol {
	 *  // nobody shakes own or spouse's hand
	 *  all p: Person | no (p + p.spouse) & p.shaken
	 *  // if p shakes q, q shakes p
	 *  all p, q: Person | p in q.shaken => q in p.shaken
	 * }
	 * 
	 * pred Puzzle() {
	 *  // everyone but Jocelyn has shaken a different number of hands
	 *  all disj p,q: Person - Jocelyn | #p.shaken != #q.shaken
	 * }
	 * 
	 * </pre>
	 */
	public Formula partition2() {
		final Formula f0 = shaken.in(Person.product(Person));
		final Variable p = Variable.unary("p");
		final Variable q = Variable.unary("q");
		final Formula f1 = p.union(p.join(spouse)).intersection(p.join(shaken)).no().forAll(p.oneOf(Person));
		final Formula f2 = p.in(q.join(shaken)).implies(q.in(p.join(shaken)))
				.forAll(p.oneOf(Person).and(q.oneOf(Person)));

		final Variable p1 = Variable.unary("p");
		final Variable q1 = Variable.unary("q");
		final Formula f = p1.eq(q1).not().implies(p1.join(shaken).count().eq(q1.join(shaken).count()).not());
		final Expression e = Person.difference(Jocelyn);
		final Formula f4 = f.forAll(p1.oneOf(e).and(q1.oneOf(e)));

		// if trying to prove theorem, add it to the formula
		final Formula f5 = counter?f4:(f4.implies((Hilary.join(shaken).count()).toExpression().eq(hypo))).not();
		
		return f0.and(f1).and(f2).and(f5);
	}

	/**
	 * Returns a bounds for the given number of persons.
	 * 
	 * @return a bounds for the given number of persons.
	 */
	public Bounds bounds1() {
		final TupleFactory f = u.factory();
		final Bounds b = new Bounds(u);
		
		final TupleSet pb = f.range(f.tuple("Hilary"), f.tuple("Person"+persons));

		// if variable, do not bound exactly
		if (var) b.bound(Person, pb);
		else b.boundExactly(Person, pb);
		
		b.boundExactly(Hilary, f.setOf("Hilary"));
		b.boundExactly(Jocelyn, f.setOf("Jocelyn"));
		b.bound(spouse, pb.product(pb));

		// if proving theorem, define the bounds of the hypothesis
		// if variable, integers are part of the universe, must also be bound
		if (!counter) {
			final TupleSet ab;
			if (var) {
				for (int i = 0; i <= maxInt(); i++)
					b.boundExactly(i, f.setOf(i));
				ab = f.range(f.tuple(Integer.valueOf(0)), f.tuple(Integer.valueOf(maxInt())));
				b.bound(hypo, ab);
			} else {
				b.boundExactly(hypo(), f.setOf(hypo()));
				ab = f.setOf(hypo());
				b.boundExactly(hypo, ab);
			}
		}
		
		return b;
	}

	public Bounds bounds2() {
		final TupleFactory f = u.factory();
		final Bounds b = new Bounds(u);
		b.bound(shaken, f.allOf(2));

		return b;
	}

	@Override
	public int getBitwidth() {
		return bits(maxInt())+1;
	}
	
	
	private int bits(int n) {
		float x = (float) (Math.log(n*2) / Math.log(2));
		int y = (int) (1 + Math.floor(x));
		return Math.max(3, y);
	}
	
	private int maxInt() {
		return persons-2;
	}
	
	private int hypo() {
		return (persons / 2) - 1;
	}

}