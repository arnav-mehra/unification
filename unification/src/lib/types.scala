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
    case Dec extends RelOp(
        (v: Int) => v - 1,
        (v: Int) => v + 1
    )
    case Rec extends RelOp(
        (_: Int) => Int.MaxValue,
        (_: Int) => Int.MaxValue
    )
}

enum LogOp {
    case Or
    case And
}

enum Ast {
    case Var  (id: Int)
    case BinOp(op: LogOp | RelOp, left: Ast, right: Ast)
    case Func (ast: Ast, args: ArrayBuffer[Int], var_cnt: Int)
}

type VarId = Int

type Var = Option[Int]
type VarSet = ArrayBuffer[Var]

type Rel = (VarId, VarId, RelOp)
type RelSet = ArrayBuffer[Rel]