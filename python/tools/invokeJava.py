import jpype
import json

jar_path = 'C:/Users/robin/Desktop/resource-simple-jar-with-dependencies.jar'
print(jar_path)

if __name__ == '__main__':
    jvmPath = jpype.getDefaultJVMPath()
    jpype.startJVM(jvmPath, '-ea', '-Djava.class.path=%s' % jar_path, convertStrings=True)
    jpype.java.lang.System.out.println("hello java!")

    clazz = jpype.JClass('ResourceFetcher')
    obj = clazz()
    result = obj.getResources('306115012')

    json_data = json.loads(result)
    print('\ncode=' + str(json_data.get('code')) + ', msg=' + str(json_data.get('msg')))
    print(type(json_data))
    jpype.shutdownJVM()
