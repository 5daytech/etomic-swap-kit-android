# Etomic Swap Kit

ETH etomic swap implementation written in Kotlin to support ETH and ERC20 atomic swaps. 

#### Please note that this project is not production ready yet!

## Swap workflow
* Bob wants to change his A coin to Alice B coin.
* Bob sends payment locked with hash of the Secret. He can refund the payment in 4 hours.
* Alice sends payment locked with Bob Secret hash. She can refund her payment in 2 hours.
* Bob spends Alice payment by revealing the secret.
* Alice spends Bob payment using revealed secret.

![Logo](/images/htlc_workflow.jpg)

## Prerequisites
* JDK >= 1.8
* Android 6 (minSdkVersion 23) or greater

## License
    MIT License

    Copyright (c) 2019
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.