package lib.types
import lib.solver.*
import scala.collection.mutable.ArrayBuffer
import scala.runtime.ScalaRunTime

type Var = Option[Int]

type VarId = Int

type VarSet = ArrayBuffer[Var]

// relation operations
enum RelOp(val fw: Int => Option[Int], val bw: Int => Option[Int]) {
    case Eq extends RelOp(
        (v: Int) => Some(v),
        (v: Int) => Some(v)
    )
    case Inc extends RelOp(
        (v: Int) => Some(v + 1),
        (v: Int) => if (v == 0) None else Some(v - 1)
    )
}

enum LogOp {
    case Or
    case And
}

enum Ast {
    case Var  (id: VarId)
    case BinOp(op: LogOp | RelOp, left: Ast, right: Ast)
    case Func (var ast: Ast, var vars: VarSet)
    case Call (callee: Ast.Func, args: ArrayBuffer[VarId])
}

enum Rel {
    case Rec(call: Ast.Call)
    case Std(left: VarId, right: VarId, rel: RelOp)

    override def toString(): String = {
        // var var_names = ArrayBuffer("x", "y", "z", "0", "xc", "yc", "x2", "y2", "z2", "0", "x2c", "y2c", "x3", "y3", "z3")
        // this match {
        //     case Rec(call) => "Rec(" + call.args.foldLeft("")((acc, id) => acc + ", [" + var_names(id) + "]").substring(2) + ")"
        //     case Std(left, right, RelOp.Eq) => "[" + var_names(left) + "] = [" + var_names(right) + "]"
        //     case Std(left, right, RelOp.Inc) => "[" + var_names(left) + "] = [" + var_names(right) + "] + 1"
        // }
        this match {
            case Rec(call) => "Rec(" + call.args.foldLeft("")((acc, id) => acc + ", [" + id + "]").substring(2) + ")"
            case Std(left, right, RelOp.Eq) => "[" + left + "] = [" + right + "]"
            case Std(left, right, RelOp.Inc) => "[" + left + "] = [" + right + "] + 1"
        }
    }
}

type RelSet = ArrayBuffer[Rel]

class RelSets(val sets: ArrayBuffer[RelSet] = ArrayBuffer()) {
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

    def prepend_to_all(new_set: RelSet): RelSets = {
        sets.foreach(_.prependAll(new_set))
        this
    }

    def sort(): RelSets = {
        sets.sortInPlaceBy(set => {
            if (set.contains(Rel.Rec)) Int.MaxValue else set.length 
        })
        this
    }

    def find_sat_set(vars: VarSet): Option[(RelSet, VarSet)] = {
        var res: Option[(RelSet, VarSet)] = None
        sets.takeWhile(set => {
            res = Solver(vars.clone(), set).solve()
            res.isEmpty
        })
        res
    }

    def print(): Unit = {
        sets.foreach(println)
    }
}