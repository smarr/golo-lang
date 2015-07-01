module Arithmetics

import java.lang.System

function gcd = |x, y, repeat| {
  var res = 0_L
  for (var i = 0_L, i < repeat, i = i + 1_L) {
    var a = x
    var b = y
    while a != b {
      if a > b {
        a = a - b
      } else {
        b = b - a
      }
    }
    res = a
  }
  return res
}

function sum = |x, y| -> x + y


function main = |args| {
  println("Hello world!")

  for (var i = 0_L, i < 1000_L, i = i + 1_L) {
      var start = System.nanoTime() / 1000_L
      gcd(1000000_L, 1312312_L, 100000_L)
      var duration = System.nanoTime() / 1000_L - start
      println("gcd: " + duration + "ms")
  }
}
