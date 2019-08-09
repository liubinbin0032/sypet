```                            
                            ____     ___      __ 
                           / __/_ __/ _ \___ / /_
                          _\ \/ // / ___/ -_) __/
                         /___/\_, /_/   \__/\__/ 
                             /___/               
      
```

#Accelerating API-based Program Synthesis via API Usage Pattern Mining

SyPet is a novel type-directed tool for component-based synthesis. The key 
novelty of our approach is the use of a compact Petri-net representation to 
model relationships between methods in an API. Given a target method signature 
S, our approach performs reachability analysis on the underlying Petri-net model 
to identify sequences of method calls that could be used to synthesize an 
implementation of S. The programs synthesized by our algorithm are guaranteed 
to type check and pass all test cases provided by the user.

This version is an improvement on top of SyPet, which can accelerate the speed of API-based program synthesis via API usage pattern mining.


#Usage

```
$ ant
$ ./run-sypet.sh benchmarks/patterns/1/benchmark1.json "-ptn -cons"


#Config file (CONFIG.json)

*blacklist: SyPet will not include those methods in the PetriNet

*poly: Specify sub-typing relationship on-demand

*buildinPkg: Build-in packages included by SyPet


#data

*deprecated: SyPet will not include those methods that is deprecated in the PetriNet

*patterns: API usage patterns mined from code snippets


#Requirements
 - JDK 1.7+
 - ANT

