# Copyright © 2004-2013 Brent Fulgham
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
#   * Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
#   * Redistributions in binary form must reproduce the above copyright notice,
#     this list of conditions and the following disclaimer in the documentation
#     and/or other materials provided with the distribution.
#
#   * Neither the name of "The Computer Language Benchmarks Game" nor the name
#     of "The Computer Language Shootout Benchmarks" nor the names of its
#     contributors may be used to endorse or promote products derived from this
#     software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# The Computer Language Benchmarks Game
# http://benchmarksgame.alioth.debian.org
#
#  contributed by Karl von Laudermann
#  modified by Jeremy Echols
#  modified by Detlef Reichl
#  modified by Joseph LaFata
#  modified by Peter Zotov

# http://benchmarksgame.alioth.debian.org/u64q/program.php?test=mandelbrot&lang=yarv&id=3

module Mandelbrot

function mandelbrot = |size| {
  var sum = 0

  var byte_acc = 0
  var bit_num = 0

  var y = 0
  while (y < intValue(size)) {
    var ci = (2.0 * doubleValue(y) / size) - 1.0

    var x = 0
    while (x < intValue(size)) {
      var zr = 0.0
      var zrzr = zr
      var zi = 0.0
      var zizi = zi
      var cr = (2.0 * doubleValue(x) / size) - 1.5

      var escape = 1

      var z = 0
      while (z < 50) {
        var tr = (zrzr - zizi) + cr
        var ti = (2.0 * zr * zi) + ci

        zr = tr
        zi = ti

        # preserve recalculation
        zrzr = zr * zr
        zizi = zi * zi

        if ((zrzr + zizi) > 4.0) {
          escape = 0
          break
        }
        z = z + 1
      }
      
      byte_acc = (byte_acc bitLSHIFT 1) bitOR escape
      bit_num = bit_num + 1

      # Code is very similar for these cases, but using separate blocks
      # ensures we skip the shifting when it's unnecessary, which is most cases.
      if (bit_num == 8) {
        sum = sum bitXOR byte_acc
        byte_acc = 0
        bit_num = 0
      } else if (x == (intValue(size) - 1)) {
        byte_acc = byte_acc bitLSHIFT (8 - bit_num)
        sum = sum bitXOR byte_acc
        byte_acc = 0
        bit_num = 0
      }
      x = x + 1
    }
    y = y + 1
  }

  return sum
}

function warmup = {
  for (var n = 0, n < 10000, n = n + 1) {
    mandelbrot(10.0)
  }
}

function sample = {
  return mandelbrot(750.0) == 192
}

function main = |args| {
  if not sample() {
    println(mandelbrot(750.0))
    println("sample verification failed")
  }

  var iterations = 100
  var warmup     = 0
  var problemSize = 1000.0

  println("Overall iterations: " + iterations)
  println("Warmup  iterations: " + warmup)
  println("Problem size:       " + problemSize)

  while warmup > 0 {
    mandelbrot(problemSize)
    warmup = warmup - 1
  }

  while iterations > 0 {
     var start = System.nanoTime()
     mandelbrot(problemSize)
     var elapsed = (System.nanoTime() - start) / 1000_L
     iterations = iterations - 1

     println("Mandelbrot: iterations=1 runtime: " +
         elapsed + "us")
  }
  
  if not sample() {
    println("sample verification failed")
  }
}
