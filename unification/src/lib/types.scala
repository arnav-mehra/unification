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
        this match {
            case Rec(call) => "Rec(" + call.args.foldLeft("")((acc, id) => acc + ", [" + id + "]").substring(2) + ")"
            case Std(left, right, RelOp.Eq) => "[" + left + "] = [" + right + "]"
            case Std(left, right, RelOp.Inc) => "[" + left + "] = [" + right + "] + 1"
        }
    }
}

type RelSet = ArrayBuffer[Rel]

object RelSets {
    def SingleRel(rs: Rel) = ArrayBuffer(ArrayBuffer(rs))
    def Empty: RelSets = ArrayBuffer(ArrayBuffer()) 
}

class RelSets extends ArrayBuffer[RelSet] {
    def and(rss2: RelSets): RelSets = {
        val new_sets = RelSets()
        for (set <- this) {
            for (set2 <- rss2) {
                new_sets.addOne(set ++ set2)
            }
        }
        new_sets
    }

    def or(rss2: RelSets): RelSets = this ++ rss2

    def append_to_all(new_set: RelSet): RelSets = {
        foreach(_.appendAll(new_set))
        this
    }

    def prepend_to_all(new_set: RelSet): RelSets = {
        foreach(_.prependAll(new_set))
        this
    }

    def sort(): RelSets = {
        sortInPlaceBy(set => {
            set.contains(Rel.Rec) match {
                case true => Int.MaxValue
                case false => set.length 
            }
        })
        this
    }

    def find_sat_set(vars: VarSet): Option[(RelSet, VarSet)] = {
        var res: Option[(RelSet, VarSet)] = None
        takeWhile(set => {
            res = Solver(vars.clone(), set).solve()
            res.isEmpty
        })
        res
    }

    def print(): Unit = foreach(println)
}