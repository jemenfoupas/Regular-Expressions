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
  private boolean willRepeat;

  public RE(String str){
    this.input = str;
    this.name = 0;
    this.started = false;
    this.ending = false;
    this.willRepeat = false;
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
    NFA baseNFA = new NFA();

    if(!baseRepeats()){
      baseNFA = base();
    }else{
      this.willRepeat = true;
      baseNFA = base();
      while(more() && peek()=='*'){
        eat('*');
      }
      this.willRepeat = false;
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
        return primitive(next());
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

  private NFA primitive(char next) {
    if((!this.willRepeat && !more()) || (this.willRepeat && !moreRepeat())){
      this.ending = true;
    }
    NFA newNFA = new NFA();
    if(!this.started){
      newNFA.addStartState(Integer.toString(this.name));
      this.name++;
      this.started = true;
    }
    if(this.ending){
      newNFA.addFinalState(Integer.toString(this.name));
      this.name++;
    }else{
      newNFA.addState(Integer.toString(this.name));
      this.name++;
    }
    if(this.willRepeat){
      newNFA.addTransition(Integer.toString(this.name - 2), 'e', Integer.toString(this.name - 1));
      newNFA.addTransition(Integer.toString(this.name - 1), next, Integer.toString(this.name - 1));
    }else{
      newNFA.addTransition(Integer.toString(this.name - 2), next, Integer.toString(this.name - 1));
    }
    
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

  /* OTHER HELPERS */
  private boolean baseRepeats(){
    boolean rtVal = false;
    String inputCopy = this.input;
    int index = 0;

    switch(inputCopy.charAt(index)){
      case '(':
        index = 1;
        while(inputCopy.charAt(index)!=')'){
          index++;
        }
        index++;
        if(inputCopy.charAt(index)=='*'){
          rtVal = true;
        }
      default:
        if(inputCopy.charAt(index+1)=='*'){
          rtVal = true;
        }
    }

    return rtVal;
  }

  private boolean moreRepeat(){
    boolean rtVal = false;
    String inputCopy = this.input;
    int index = 0;

    while(index<inputCopy.length() && inputCopy.charAt(index)=='*'){
      index++;
    }
    if(index!=inputCopy.length()){
      rtVal = true;
    }

    return rtVal;
  }
}