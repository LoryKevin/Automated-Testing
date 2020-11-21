import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ByMethod {
    //缓存.dot内容
    public static ArrayList<String> methodDot = new ArrayList<>();
    //缓存方法间依赖信息
    public static ArrayList<String> methodRelation = new ArrayList<>();
    //缓存被选出的方法
    public static ArrayList<String> selectMethod = new ArrayList<>();


    public static void byMethod(CHACallGraph cg,ArrayList<String> change_info){
        //生成method.dot
        getMethodDot(cg);

        //README里的方法
        for(CGNode node:cg){
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if(node.getMethod() instanceof ShrikeBTMethod){
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod)node.getMethod();
                if("Application".equals(method.getDeclaringClass().getClassLoader().toString())){
                    // 获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    // 获取方法签名
                    String signature = method.getSignature();
                    if(signature.contains("Test") && judgeMethod(cg,node,change_info) && !signature.contains("<init>") && !selectMethod.contains(classInnerName+" "+signature)){
                        selectMethod.add(classInnerName+" "+signature);
                    }
                }
            }
        }

        CentralNode.writeFile(selectMethod,"selection-method.txt");
        System.out.println("selection-method.txt build complete");
    }

    //循环迭代判断方法是否受到影响
    private static Boolean judgeMethod(CHACallGraph cg, CGNode node, List<String> changes){
        //后继节点
        Iterator<CGNode> succNode = cg.getSuccNodes(node);
        //节点栈
        ArrayList<CGNode> nodeStack = new ArrayList<>();
        //压入当前节点
        nodeStack.add(node);
        //指针
        int pointer = 1;
        if(!succNode.hasNext()) {
            return false;
        }
        //子节点
        CGNode sub;
        while (true){ //通过continue、break和return控制循环
            if(succNode.hasNext()){
                sub = succNode.next();
                if (!"Application".equals(sub.getMethod().getDeclaringClass().getClassLoader().toString())) continue;
                if(!nodeStack.contains(sub)){
                    nodeStack.add(sub);
                }else {
                    continue;
                }
            }else if(pointer<nodeStack.size()){
                sub = nodeStack.get(pointer);
                pointer++;
                succNode = cg.getSuccNodes(sub);
                continue;
            }else{
                break;
            }
            String succSignature = sub.getMethod().getSignature();
            for(String change:changes){
                if(change.split(" ")[1].equals(succSignature)){
                    return true;
                }
            }
        }
        return false;
    }

    public static void getMethodDot(CHACallGraph cg){
        methodDot.add("digraph method {"); //title
        //README里的方法
        for(CGNode node:cg){
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if(node.getMethod() instanceof ShrikeBTMethod){
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    // 获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    // 获取方法签名
                    String signature = method.getSignature();
                    if(classInnerName.contains("Test")||classInnerName.contains("$"))continue;
                    Iterator<CGNode> predNodes= cg.getPredNodes(node);
                    while(predNodes.hasNext()){
                        CGNode next =predNodes.next();
                        if("Application".equals(next.getMethod().getDeclaringClass().getClassLoader().toString())) {
                            String nextSignature=next.getMethod().getSignature();
                            if(!methodDot.contains("\t\""+signature+"\" -> \""+nextSignature+"\";")){
                                methodDot.add("\t\""+signature+"\" -> \""+nextSignature+"\";");
                                methodRelation.add(signature+" "+nextSignature);
                            }
                        }
                    }
                }
            }
        }

        methodDot.add("}"); //end
        CentralNode.writeFile(methodDot,"method.dot");
        System.out.println("method.dot build complete");
    }

}
