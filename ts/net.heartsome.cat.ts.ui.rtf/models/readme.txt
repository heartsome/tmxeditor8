如非必要，建议不要修改 RTF 模板文件，以免造成生成 RTF 文件时出错。
如要修改 RTF 模板文件，请注意先备份，然后将 RTFTemplate.dot 放到 C:\Documents and Settings\username\Application Data\Microsoft\Word\Startup 目录下（username 为登录系统的用户名）,
这样每次打开 Word 时就会出现 RTFTemplate 的一个菜单。
在修改完成后，再用文本编辑器打开此文件，将模板里面所有的 fcharset0 替换为 fcharset134，否则中文会出现乱码。