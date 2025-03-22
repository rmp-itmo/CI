rm -R classes
mkdir classes
clojure -M -e "(compile 'core)"
bb -cp $(clojure -Spath) uberjar CI.jar -m core 
