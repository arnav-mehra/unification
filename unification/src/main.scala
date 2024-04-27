package main
import lib.types.*
import lib.solver.*
import lib.parser.*
import lib.unifier.*
import scala.collection.mutable.ArrayBuffer

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      println("Please add a file to run")
      System.exit(1)
    }

    val fname: String = args(0) + ".aunif"
    println("Running: " + fname)
    
    val code: String = scala.io.Source.fromFile(fname).mkString
    println(code)

    val ast: Ast = ProgramParser.parse(code)
    println(ast)

    // var func: Ast.Func = Ast.Func("shit", Ast.Var(0), ArrayBuffer())

    // func.ast = Ast.BinOp(LogOp.Or,
    //   Ast.BinOp(LogOp.And,
    //     Ast.BinOp(RelOp.Eq, Ast.Var(0), Ast.Var(3)), // x = 0
    //     Ast.BinOp(RelOp.Eq, Ast.Var(1), Ast.Var(2))  // y = z
    //   ),
    //   Ast.BinOp(LogOp.And,
    //     Ast.BinOp(RelOp.Inc, Ast.Var(0), Ast.Var(4)), // x = xc + 1
    //     Ast.BinOp(LogOp.And,
    //       Ast.BinOp(RelOp.Inc, Ast.Var(5), Ast.Var(1)), // yc = y + 1
    //       Ast.Call(func, ArrayBuffer(4, 5, 2)), // func(xc, yc, z)
    //     )
    //   )
    // )

    // func.vars = ArrayBuffer(
    //   None, None, None, // x, y, z
    //   Some(0), None, None // 0, xc, yc
    // )

    // var args: VarSet = ArrayBuffer(Some(2), None, Some(3))
    // Unifier(func, args).unify()
  }
}