package main
import lib.types.*
import lib.solver.*
import lib.unifier.*
import scala.collection.mutable.ArrayBuffer

object Main {
  def main(args: Array[String]): Unit = {
    var func: Ast.Func = Ast.Func(Ast.Var(0), ArrayBuffer())

    func.ast = Ast.BinOp(LogOp.Or,
      Ast.BinOp(LogOp.And,
        Ast.BinOp(RelOp.Eq, Ast.Var(0), Ast.Var(3)), // x = 0
        Ast.BinOp(RelOp.Eq, Ast.Var(1), Ast.Var(2))  // y = z
      ),
      Ast.BinOp(LogOp.And,
        Ast.BinOp(RelOp.Inc, Ast.Var(0), Ast.Var(4)), // x = xc + 1
        Ast.BinOp(LogOp.And,
          Ast.BinOp(RelOp.Inc, Ast.Var(5), Ast.Var(1)), // yc = y + 1
          Ast.Call(func, ArrayBuffer(4, 5, 2)), // func(xc, yc, z)
        )
      )
    )

    func.vars = ArrayBuffer(
      None, None, None, // x, y, z
      Some(0), None, None // 0, xc, yc
    )

    var args: VarSet = ArrayBuffer(Some(3), None, Some(2))
    Unifier(func, args).unify()
  }
}