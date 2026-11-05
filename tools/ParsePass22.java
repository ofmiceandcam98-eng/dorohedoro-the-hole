import javax.tools.*;
import com.sun.source.util.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
public class ParsePass22 {
 public static void main(String[] args) throws Exception {
  var files = new ArrayList<File>();
  try(var paths=Files.walk(Path.of("src/main/java"))) {
   paths.filter(f->f.toString().endsWith(".java")).forEach(f->files.add(f.toFile()));
  }
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
  DiagnosticCollector<JavaFileObject> collector=new DiagnosticCollector<>();
  try(var fm=compiler.getStandardFileManager(collector,null,null)) {
   var task=(JavacTask)compiler.getTask(null,fm,collector,List.of("--release","21","-proc:none"),null,fm.getJavaFileObjectsFromFiles(files));
   task.parse();
  }
  var errors=collector.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).toList();
  errors.forEach(d->System.out.println(d));
  System.out.printf("JDK 21 syntax parser: %d files, %d parse errors%n",files.size(),errors.size());
  if(!errors.isEmpty())System.exit(1);
 }
}
