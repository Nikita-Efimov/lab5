test_args = storage.xml
compile_options = -XDignore.symbol.file
src_dir = src/
preprocessed_dir = preprocessed/
binaries_dir = bin/
lib_path = lib/
class_path = $(lib_path)*:.
startup_string = java -cp $(class_path):$(binaries_dir) Main

# Startups
test: $(binaries_dir)*.class
	######## START #########
	$(startup_string) $(test_args)

run: $(binaries_dir)*.class
	######## START #########
	$(startup_string)

# Java doc
docs: $(preprocessed_dir)*.java
	mkdir docs
	javadoc -cp $(lib_path)* $(preprocessed_dir)* -d docs

# Clear
clear:
	rm -f $(binaries_dir)* $(preprocessed_dir)*

# Preprocessing
$(preprocessed_dir)*.java: $(src_dir)*.java
	# Copy all
	cp -rf $(src_dir)* $(preprocessed_dir)
	# Do smth by hands
	cpp -P $(src_dir)CmdWorker.java $(preprocessed_dir)CmdWorker.java
	cpp -P $(src_dir)DiagnosticSignalHandler.java $(preprocessed_dir)DiagnosticSignalHandler.java
	cpp -P $(src_dir)Main.java $(preprocessed_dir)Main.java

# Compiling
$(binaries_dir)*.class: $(preprocessed_dir)*.java
	javac -cp $(class_path) $(preprocessed_dir)*.java -d $(binaries_dir) $(compile_options)
