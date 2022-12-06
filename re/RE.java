package re;

import fa.dfa.DFA;
import fa.nfa.NFA;
import fa.State;
import fa.nfa.NFAState;

public class RE implements REInterface{

  private String input ;

  private int name;
  private boolean started;
  private boolean ending;

  public RE(String str){
    this.input = str;
    this.name = 0;
    this.started = false;
    this.ending = false;
  }

  @Override
  public NFA getNFA() {
    NFA nfa = regex();
    return nfa;
  }

  /* REGEX TERM TYPES */

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

  private NFA term() {
    NFA factorNFA = new NFA();

    while (more() && peek() != ')' && peek() != '|') {
      NFA nextFactor = factor();
      factorNFA = Sequence(factorNFA,nextFactor) ;
    }

    return factorNFA;
  }

  private NFA factor() {
    NFA baseNFA = base();

    while (more() && peek() == '*') {
      eat('*') ;
      baseNFA = Repetition(baseNFA) ;
    }

    return baseNFA;
  }

  private NFA base() {
    switch (peek()) {
      case '(':
        eat('(') ;
        NFA r = regex() ;  
        eat(')') ;
        return r ;
      default: 
        return Primitive(next());
    }
  }

  /* REGEX SUBBUILDERS */

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

  public NFA Repetition(NFA internal) {
    for(State state: internal.getFinalStates()) {
      internal.addTransition(state.getName(), internal.getABC().iterator().next(), state.getName());
    }
    return internal;
  }

  private NFA Primitive(char next) {
    NFA newNFA = new NFA();
    if(!this.started){
      newNFA.addStartState(Integer.toString(this.name));
      this.name++;
    }
    if(this.ending){
      newNFA.addFinalState(Integer.toString(this.name));
      this.name++;
    }else{
      newNFA.addState(Integer.toString(this.name));
      this.name++;
    }
    newNFA.addTransition(Integer.toString(this.name - 2), next, Integer.toString(this.name - 1));
    return newNFA;
  }

  /* DECENT PARSING INTERNALS */
  
  private char peek() {
    return input.charAt(0) ;
  }
    
  private void eat(char c) {
    if (peek() == c) {
      this.input = this.input.substring(1);
    } else {
      throw new RuntimeException("Expected: " + c + "; got: " + peek()) ;
    }
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