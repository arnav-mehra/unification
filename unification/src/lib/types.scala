package lib.types
import scala.collection.mutable.ArrayBuffer

enum RelOp(val fw: Int => Int, val bw: Int => Int) {
    case Eq extends RelOp(
        (v: Int) => v,
        (v: Int) => v
    )
    case Inc extends RelOp(
        (v: Int) => v + 1,
        (v: Int) => v - 1
    )
}

enum LogOp {
    case Or
    case And
}

enum Ast {
    case Var  (id: Int)
    case BinOp(op: LogOp | RelOp, left: Ast, right: Ast)
    case Func (ast: Ast, vars: VarSet)
    case Call (fn: Ast, args: ArrayBuffer[Int])
}

type VarId = Int

type Var = Option[Int]

type VarSet = ArrayBuffer[Var]

enum Rel(right: VarId, left: VarId, rel: RelOp | Ast) {
    case Rec(fn: Ast) extends Rel(
        Int.MaxValue,
        Int.MaxValue,
        fn
    )
}

type RelSet = ArrayBuffer[Rel]

class RelSets(val sets: ArrayBuffer[RelSet]) {
    def and(rss2: RelSets): RelSets = {
        val new_sets: ArrayBuffer[RelSet] = ArrayBuffer()
        for (set <- sets) {
            for (set2 <- rss2.sets) {
                new_sets.addOne(set ++ set2)
            }
        }
        RelSets(new_sets)
    }

    def or(rss2: RelSets): RelSets = {
        RelSets(sets ++ rss2.sets)
    }

    def append_to_all(new_set: RelSet): RelSets = {
        sets.foreach(_.appendAll(new_set))
        this
    }

    def sort(): RelSets = {
        sets.sortInPlaceBy(set => {
            if (set.contains(Rel.Rec)) Int.MaxValue else set.length 
        })
        this
    }

    def find_sat_set(vars: VarSet): RelSet = {
        sets.find(set => Solver(vars.clone(), set).solve())
    }
}