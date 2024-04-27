package lib.parser

import scala.util.parsing.combinator.JavaTokenParsers
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import lib.types.*

object Indexer {
    val fn_map: HashMap[String, (Ast.Func, HashMap[String, Int])] = HashMap()

    def get_fn(fname: String): Ast.Func = {
        fn_map(fname)
    }

    // def convert(ast: Ast): Ast = {
    //     Ast.FuncStandIn
    // }
}

class FunctionIndexer {
    val var_map: HashMap[String, Ast.Var] = HashMap()

    def get_var(var_str: String): Ast.Var = {
        if (!var_map.contains(var_str)) {
            var v = Ast.Var(var_map.size)
            var_map.addOne(var_str, v) 
        }
        var_map(var_str)
    }

    def digest(fn_ast: Ast.FuncStandIn): Unit = {
        recur_index_vars(fn_ast)
    }

    def recur_index_vars(ast: Ast): Unit = {
        ast match {
            case Ast.VarStandIn(name) => get_var(name)
            case Ast.FuncStandIn(name, ast, vars) => {
                vars.foreach(v => get_var(v.name))
            }
            case Ast.CallStandIn(name, args) => {
                args.foreach(v => get_var(v.name))
            }
            case Ast.BinOp(op, left, right) => {
                recur_index_vars(left)
                recur_index_vars(right)
            }
            case _ => {}
        }   
    }
}

object ProgramParser extends JavaTokenParsers {
    def parse(code: String): Ast.FuncStandIn = {
        parseAll(func_dec, code).get
    }

    def func_dec: Parser[Ast.FuncStandIn] = variable ~ rep(var_ast) ~ "=>" ~ expr_ast ^^ {
        case fname~ls~"=>"~ast => Ast.FuncStandIn(fname, ast, ls.to(ArrayBuffer))
    }

    def expr_ast: Parser[Ast] = (log_ast | rel_ast | call_ast)

    def log_ast: Parser[Ast] = "(" ~ expr_ast ~ rep(log_op ~ expr_ast) ~ ")" ^^ {
        case "("~left~ls~")" => {
            ls.foldLeft(left)((acc, right) =>
                Ast.BinOp(right._1, acc, right._2)
            )
        }
    }

    def rel_ast: Parser[Ast.BinOp] = var_ast ~ rel_op ~ var_ast ^^ {
        case vl~op~vr => Ast.BinOp(op, vl, vr)
    }

    def call_ast: Parser[Ast.CallStandIn] = variable ~ rep(var_ast) ^^ {
        case fn_name~arg_ls => Ast.CallStandIn(fn_name, arg_ls.to(ArrayBuffer))
    }

    def var_ast: Parser[Ast.VarStandIn] = (variable | literal) ^^ {
        s => Ast.VarStandIn(s)
    }

    def log_op: Parser[LogOp] = ("|" | "&") ^^ {
        case "|" => LogOp.Or
        case "&" => LogOp.And
    }

    def rel_op: Parser[RelOp] = ("=" | ">") ^^ {
        case "=" => RelOp.Eq
        case ">" => RelOp.Inc
    }

    def variable: Parser[String] = "[a-zA-Z_][a-zA-Z_0-9]*".r
    
    def literal: Parser[String] = number
    def number: Parser[String] = "\\d+".r
}