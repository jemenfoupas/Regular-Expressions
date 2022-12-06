package re;

import fa.dfa.DFA;
import fa.nfa.NFA;
import fa.State;
import fa.nfa.NFAState;

public class RE implements REInterface{

  private NFA nfa;
  private String input ;

  private int name;

  public RE(String str){
      this.input = str;
      this.name = 1;
  }

  @Override
  public NFA getNFA() {
      NFA nfa = regex();
      return nfa;
  }

  private NFA regex() {
    NFA nfaTerm = term();

    if (more() && peek() == '|') {
      eat ('|') ;
      NFA regexNFA = regex();
      NFA union = union(nfaTerm, regexNFA) ;
      return union;
    } else {
      return nfaTerm ;
    }
  }

  private NFA union(NFA first, NFA second) {
    NFA newNFA = new NFA();
    newNFA.addStartState(Integer.toString(this.name));
    this.name = this.name + 1;

    newNFA.addNFAStates(first.getStates());
    newNFA.addNFAStates(second.getStates());
    
    newNFA.addTransition(newNFA.getStartState().getName(), 'e', first.getStartState().getName());
    newNFA.addTransition(newNFA.getStartState().getName(), 'e', second.getStartState().getName());
    return newNFA;
  }

  private NFA term() {
    NFA factorNFA = new NFA();

    while (more() && peek() != ')' && peek() != '|') {
      NFA nextFactor = factor();
      factorNFA = Sequence(factorNFA,nextFactor) ;
    }
    return factorNFA;
  }

  private NFA Sequence(NFA first, NFA second) {
    NFA newNFA = new NFA();

    newNFA.addNFAStates(first.getStates());
    newNFA.addNFAStates(second.getStates());

    for(State state: first.getFinalStates()) {
      // state.setNonFinal();
      newNFA.addTransition(state.getName(), 'e', second.getStartState().getName());
    }
    return newNFA;
  }

  private NFA factor() {
    NFA baseNFA = base();

    while (more() && peek() == '*') {
      eat('*') ;
      baseNFA = Repetition(baseNFA) ;
    }
    return baseNFA;
  }

  public NFA Repetition(NFA internal) {
    for(State state: internal.getFinalStates()) {
      internal.addTransition(state.getName(), internal.getABC().iterator().next(), state.getName());
    }
    return internal;
  }

  private NFA base() {
    switch (peek()) {
      case '(':
        eat('(') ;
        NFA r = regex() ;  
        eat(')') ;
      return r ;
      default: return Primitive(next());
    }
  }


  private NFA Primitive(char next) {
    NFA newNFA = new NFA();
    newNFA.addStartState(Integer.toString(this.name));
    this.name += 1;
    newNFA.addFinalState(Integer.toString(this.name));
    this.name += 1;
    newNFA.addTransition(Integer.toString(this.name - 2), next, Integer.toString(this.name - 1));
    return newNFA;
  }

  private char peek() {
        return input.charAt(0) ;
      }
    
  private void eat(char c) {
    if (peek() == c)
      this.input = this.input.substring(1) ;
    else
      throw new 
        RuntimeException("Expected: " + c + "; got: " + peek()) ;
  }
  
  private char next() {
    char c = peek() ;
    eat(c) ;
    return c ;
  }

  private boolean more() {
    return input.length() > 0 ;
  }
    
}