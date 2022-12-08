package re;

import fa.dfa.DFA;
import fa.nfa.NFA;

import java.util.HashSet;
import java.util.Set;

import fa.State;
import fa.nfa.NFAState;

public class RE implements REInterface{

  private String input ;

  private int name;
  private boolean started;
  private boolean ending;
  private boolean willRepeat;
  private boolean inSequence;
  private int unionCount;

  public RE(String str){
    this.input = str;
    this.name = 0;
    this.unionCount = 0;
    this.started = false;
    this.ending = false;
    this.willRepeat = false;
    this.inSequence = false;
  }

  @Override
  public NFA getNFA() {
    NFA nfa = regex();

    return nfa;
  }

  /* REGEX TERM TYPES */

  private NFA regex() {
    NFA nfaTerm = term();

    //System.out.println("regex \n"+input+"\n"+nfaTerm);

    if (more() && peek() == '|') {
      eat ('|') ;
      this.started = false;
      NFA regexNFA = regex();
      nfaTerm = union(nfaTerm, regexNFA) ;

      //System.out.println("regex After union \n"+input+"\n"+nfaTerm);
      return nfaTerm;
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
    newNFA.addTransition(first.getStartState().getName(), 'e', second.getStartState().getName());
    newNFA.addTransition(second.getStartState().getName(), 'e', first.getStartState().getName());
    newNFA.addAbc(first.getABC());
    newNFA.addAbc(second.getABC());
    return newNFA;
  }

  private NFA term() {
    NFA factorNFA = factor();

    //System.out.println("term \n"+input+"\n"+factorNFA);
    
    while (more() && peek() != ')' && peek() != '|') {
      this.inSequence = true;
      NFA nextFactor = factor();
      this.inSequence = false;
      factorNFA = sequence(factorNFA,nextFactor) ;
      //System.out.println("term after sequence\n"+input+"\n"+factorNFA);
    }

    return factorNFA;
  }

  private NFA sequence(NFA first, NFA second) {

    //System.out.println("sequence first \n"+first);
    //System.out.println("sequence second \n"+second);

    first.addNFAStates(second.getStates());
    first.addAbc(second.getABC());
    //System.out.println("sequence  before for loop \n"+first);

    for(State state: first.getFinalStates()){
      if(!isInNFA(state.getName(), second)){
        first.addTransition(state.getName(), 'e',second.getStartState().getName());
        ((NFAState) state).setNonFinal();
        System.out.println("sequence for loop \n"+first);
      }
      
    }

    

    //System.out.println("sequence first 2\n"+first);
    //System.out.println("sequence second 2\n"+second);
    return first;
  }


  private boolean isInNFA(String name,NFA nfa) {
    for(State state : nfa.getFinalStates())
      if(state.getName().equals(name))
        return true;
    return false;
  }

  private NFA factor() {
    NFA baseNFA;

    if(!baseRepeats()){
      baseNFA = base();
      //System.out.println("factor \n"+input+"\n"+baseNFA);
    }else{
      this.willRepeat = true;
      baseNFA = base();
      while(more() && peek()=='*'){
        eat('*');
      }
      this.willRepeat = false;
      //System.out.println("factor not repeat \n"+input+"\n"+baseNFA);
    }

    return baseNFA;
  }

  private NFA base() {
    switch (peek()) {
      case '(':
        eat('(') ;
        NFA r = regex() ;
        //System.out.println("base \n"+input+"\n"+r);  
        eat(')') ;
        return r ;
      default: 
        NFA nfa = primitive(next());
        //System.out.println("base \n"+nfa);  
        return nfa;
    }
  }

  /* REGEX SUBBUILDERS */
/* 
 private NFA union(NFA first, NFA second) {
    NFA newNFA = new NFA();
    Set<NFAState> firstStates = new HashSet<NFAState>();
    Set<NFAState> secondStates = new HashSet<NFAState>();

    newNFA.addStartState(Integer.toString(-1-this.unionCount));
    this.unionCount++;

    for(State s : first.getStates()){
      NFAState ns = (NFAState)s;
      firstStates.add(ns);
    }
    for(State s : second.getStates()){
      NFAState ns = (NFAState)s;
      secondStates.add(ns);
    }
    for(NFAState s : firstStates){
      if(s.isFinal()){
        newNFA.addFinalState(s.getName());
      }else{
        newNFA.addState(s.getName());
      }
    }
    for(NFAState s : secondStates){
      if(s.isFinal()){
        newNFA.addFinalState(s.getName());
      }else{
        newNFA.addState(s.getName());
      }
    }
    for(char c : first.getABC()){
      for(NFAState s : firstStates){
        for(State state : first.getToState(s,c)){
          newNFA.addTransition(s.getName(), c, state.getName());
        }
      }
    }
    for(NFAState s : firstStates){
      for(State state : first.getToState(s,'e')){
        newNFA.addTransition(s.getName(), 'e', state.getName());
      }
    }
    for(char c : second.getABC()){
      for(NFAState s : secondStates){
        for(State state : second.getToState(s, c)){
          newNFA.addTransition(s.getName(), c, state.getName());
        }
      }
    }
    for(NFAState s : secondStates){
      for(State state : second.getToState(s, 'e')){
        newNFA.addTransition(s.getName(), 'e', state.getName());
      }
    }
    
    newNFA.addTransition(newNFA.getStartState().getName(), 'e', first.getStartState().getName());
    newNFA.addTransition(newNFA.getStartState().getName(), 'e', second.getStartState().getName());

    //newNFA = eCloseStart(newNFA);

    return newNFA;
  }

  private NFA sequence(NFA first, NFA second) {
    NFA newNFA = new NFA();
    boolean hasState = false;
    HashSet<NFAState> firstStates = new HashSet<NFAState>();
    HashSet<NFAState> secondStates = new HashSet<NFAState>();

    for(State s : first.getStates()){
      NFAState ns = (NFAState)s;
      firstStates.add(ns);
    }
    for(State s : second.getStates()){
      NFAState ns = (NFAState)s;
      secondStates.add(ns);
    }

    for(NFAState s : firstStates){
      if(s.getName().equals(first.getStartState().getName())){
        newNFA.addStartState(s.getName());
      }else if(s.isFinal()){
        newNFA.addFinalState(s.getName());
      }else{
        newNFA.addState(s.getName());
      }
    }
    for(char c : first.getABC()){
      for(NFAState s : firstStates){
        for(State state : first.getToState(s,c)){
          newNFA.addTransition(s.getName(), c, state.getName());
        }
      }
    }
    for(NFAState s : firstStates){
      for(State state : first.getToState(s,'e')){
        newNFA.addTransition(s.getName(), 'e', state.getName());
      }
    }
    for(State s : second.getStates()){
      hasState = false;
      for(State nState : newNFA.getStates()){
        if(s.getName().equals(nState.getName())){
          hasState = true;
        }
      }
      if(!hasState){
        if(this.ending){
          newNFA.addFinalState(s.getName());
        }else{
          newNFA.addState(s.getName());
        }
      }
    }
    for(char c : second.getABC()){
      for(NFAState s : secondStates){
        for(State state : second.getToState(s, c)){
          newNFA.addTransition(s.getName(), c, state.getName());
        }
      }
    }
    for(NFAState s : secondStates){
      for(State state : second.getToState(s, 'e')){
        newNFA.addTransition(s.getName(), 'e', state.getName());
      }
    }

    return newNFA;
  }
 */
  public NFA Repetition(NFA internal) {
    for(State state: internal.getFinalStates()) {
      internal.addTransition(state.getName(), internal.getABC().iterator().next(), state.getName());
    }
    return internal;
  }

  private NFA primitive(char next) {
    NFA newNFA = new NFA();
    if((!this.willRepeat && !more()) || (this.willRepeat && !moreRepeat())){
      this.ending = true;
    }

    newNFA.addStartState(Integer.toString(this.name));
    this.name++;  
    ((NFAState) newNFA.getStartState()).setFinal();

    System.out.println("primitive 4 \n"+newNFA);  
    /*if(!this.inSequence){
      if(!this.started){
        newNFA.addStartState(Integer.toString(this.name));
        this.name++;
        this.started = true;
      }else{
        newNFA.addState(Integer.toString(this.name));
        this.name++;
      }
      if(this.ending){
        newNFA.addFinalState(Integer.toString(this.name));
        this.name++;
      }else{
        newNFA.addState(Integer.toString(this.name));
        this.name++;
      }
    }else{
      newNFA.addState(Integer.toString(this.name-1));
      if(this.ending){
        newNFA.addFinalState(Integer.toString(this.name));
        this.name++;
      }else{
        newNFA.addState(Integer.toString(this.name));
        this.name++;
      }
    }
    if(this.ending){
      newNFA.addFinalState(Integer.toString(this.name));
      this.name++;
    }else{
      newNFA.addState(Integer.toString(this.name));
      this.name++;
    }*/
    if(this.willRepeat){
      //newNFA.addTransition(Integer.toString(this.name - 2), 'e', Integer.toString(this.name - 1));
      newNFA.addTransition(Integer.toString(this.name - 1), next, Integer.toString(this.name - 1));
    }else{
	  //newNFA.addTransition(Integer.toString(this.name - 2), next, Integer.toString(this.name - 1));
      NFA switchNFA =  new NFA();
      switchNFA.addStartState(Integer.toString(this.name));
      this.name++; 
      switchNFA.addNFAStates(newNFA.getStates());

      switchNFA.addTransition(Integer.toString(this.name - 1), next, Integer.toString(this.name - 2));
      newNFA = switchNFA;
    }

    //System.out.println("primitive 5 \n"+newNFA);  
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
        if((index+1)<inputCopy.length()){
          if(inputCopy.charAt(index+1)=='*'){
            rtVal = true;
          }
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
  /* 
  private NFA eCloseStart(NFA nfa){
    NFA newNFA = new NFA();
    int newName = 0;
    Set<NFAState> oldStates = new HashSet<NFAState>();

    for(State s : nfa.getStates()){
      NFAState ns = (NFAState)s;
      oldStates.add(ns);
    }

    newNFA.addStartState(Integer.toString(newName));
    newName++;

    return newNFA;
  }
  */
}