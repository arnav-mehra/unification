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
    case Func (name: String, var ast: Ast, var vars: VarSet)
    case Call (callee: Ast.Func, args: ArrayBuffer[VarId])
    case Query(callee: Ast.Func, args: VarSet)

    override def toString(): String = {
        this match {
            case Call(callee, args) => callee.name + "(" + args.foldLeft("")((acc, id) => acc + ", [" + id + "]").substring(2) + ")"
            case _ => ScalaRunTime._toString(this)
        }
    }
}

enum Rel {
    case Rec(caller: Ast.Func, call: Ast.Call)
    case Std(left: VarId, right: VarId, rel: RelOp)

    override def toString(): String = {
        this match {
            case Rec(_, call) => call.callee.name + "(" + call.args.foldLeft("")((acc, id) => acc + ", [" + id + "]").substring(2) + ")"
            case Std(left, right, RelOp.Eq) => "[" + left + "] = [" + right + "]"
            case Std(left, right, RelOp.Inc) => "[" + left + "] = [" + right + "] + 1"
        }
    }
}

type RelSet = ArrayBuffer[Rel]

object RelSets {
    def SingleRel(rs: Rel): RelSets = {
        var rss = RelSets()
        rss.append(ArrayBuffer(rs))
        rss
    }
    def Empty: RelSets = {
        var rss = RelSets()
        rss.append(ArrayBuffer())
        rss
    }
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

    def or(rss2: RelSets): RelSets = this.addAll(rss2)

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