## git 自定义命令行
如果感觉命令多了记不住，那就为git配置两个别名，比如：
```shell script
git config --global alias.unstage 'reset HEAD --'
git config --global alias.restore 'checkout --'
```

我们拿 README.md 这个文件举例，比如修改了一段文字描述，想恢复回原来的样子：
```shell script
git restore README.md
```

执行上面命令即可，
如果修改已经被 git add README.md 放入暂存队列，那就要 依次执行 如下两条命令：
```shell script
git unstage README.md
git restore README.md
```

---
## git-br 本质为如下脚本
```shell script
function branches() {
    branch=""
    branches=`git branch --list`
    while read -r branch; do
    clean_branch_name=${branch//\*\ /}
    description=`git config branch.$clean_branch_name.description`
    printf "%-15s %s\n" "$branch" "$description"
    done <<< "$branches"
}
```
