#!/bin/bash
BinAdd="DomainAdaptation/bin"
MalletAdd="DomainAdaptation/lib/*"
StopList="DomainAdaptation/stoplists"
src=en
trg=fa
OutFolder="outputs-path-address"
src_in="source-in-domain-corpus-address"
src_out="source-out-domain-corpus-address"
trg_in="target-in-domain-corpus-address"
trg_out="target-out-domain-corpus-address"
method="src"  ## src | trg | src-trg

mkdir $OutFolder


###################################################################################
###      ScoreSentences -> score the sentences in the out domain corpora	###
### Parameters:									###		
##   --method   src|trg|src-trg (which side of corpus is used for scoring )	###
##   --src-in   source-in-domain-corpus-address			 		###
##   --trg-in   target-in-domain-corpus-address			 		###
##   --src-out  source-out-domain-corpus-address			 	###
##   --trg-out  target-out-domain-corpus-address				###
##   --src-stopList source-stopwords-list-address				###
##   --trg-stopList target-stopwords-list-address				###
##   --output-path  outputs-path-address					###
##   --topics   number-of-topics (default 100)					###
##   --iteration  number-of-iterations-for-training-topic-models (default 1000)	###
##   --alpha 	alpha-parameter-for-training-topic-models (default 1.)		###
##   --beta   	beta-parameter-for-training-topic-models (default 0.001)	###
##   --topic-words  number-of-each-topic's-top-words-used-for-scoring (def 50)	###
### Outputs:									###
##  	 topic models files + scores file					###   
###################################################################################

java -Xmx10g -cp "$BinAdd:$MalletAdd" ScoreSentences --src-in $src_in --src-out $src_out --trg-in $trg_in --trg-out $trg_out --src-stopList $StopList/$src.txt --trg-stopList $StopList/$trg.txt --output-path $OutFolder --method $method

ScoresAdd=$OutFolder/scores

###################################################################################
###      SelectSentences -> select the top sentences with positive scores  	###
### Parameters:									###
##   --src-out  source-out-domain-corpus-address			 	###
##   --trg-out  target-out-domain-corpus-address				###
##   --scores   address-of-the-scores-file-computed-in-previous-step		###
##   --method   src|trg|src-trg (which side of corpus was used for scoring )	###
##   --output-path  outputs-path-address					###
##   -n 	maximum-number-of-sentences-to-be-selected			###
##   --weight-in    weight-of-the-in-domain-score (default 1)			###
##   --weight-out    weight-of-the-out-domain-score (default 1)			###
##										###
### Outputs:									###
##   corpusName.selected files contain the selected sentences			###
##   corpusName.notselected files contain the remained not selected sentences	###
##										###
###################################################################################

java -Xmx10g -cp "$BinAdd:$MalletAdd" SelectSentences --src-out $src_out --trg-out $trg_out --scores $ScoresAdd --output-path $OutFolder -n 500000 --method $method

