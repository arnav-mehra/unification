package main
import lib.types.*
import lib.solver.*
import lib.unifier.*
import scala.collection.mutable.ArrayBuffer

object Main {
  def main(args: Array[String]): Unit = {
    var func = Ast.Func(
      Ast.BinOp(LogOp.And,
        Ast.BinOp(LogOp.Or,
          Ast.BinOp(
            RelOp.Eq,
            Ast.Var(0),
            Ast.Var(1)
          ),
          Ast.BinOp(
            RelOp.Eq,
            Ast.Var(1),
            Ast.Var(2)
          )
        ),
        Ast.BinOp(LogOp.Or,
          Ast.BinOp(
            RelOp.Eq,
            Ast.Var(1),
            Ast.Var(0)
          ),
          Ast.BinOp(
            RelOp.Eq,
            Ast.Var(2),
            Ast.Var(1)
          )
        )
      ),
      ArrayBuffer(),
      ArrayBuffer(Option.empty, Option.empty, Option.empty)
    )
      
    var args: VarSet = ArrayBuffer(Option.empty, Option.empty, Option.empty)

    var sol = Unifier(func, args).unify()
    println(sol)
  }
}