import groovy.text.GStringTemplateEngine

class Menu {
  String name
  MenuItem[] items
}

class MenuItem {
  String url, title, pageid
  SubMenuItem[] items

  boolean containsSubMenuItem(String pageid) {
    for (def item in items) {
      if (item.pageid == pageid) {
        return true;
      }
    }

    false
  }
}

class SubMenuItem {
  String url, title, pageid
}

class Page {
  String pageid, dir
}

//=======================================================

def lastupdate = String.format("%te %<tB %<tY", new GregorianCalendar())

def basedir = './'

def templatecontent = new File(basedir + 'site/pagetemplate.html').text

def menus = [
        new Menu(name: 'Menu', items: [
                new MenuItem(title: 'Overview', pageid: 'overview'),
                new MenuItem(title: '60 Second guide', pageid: '60second'),
                new MenuItem(title: 'Download', pageid: 'download'),
                new MenuItem(title: 'Sponsors', pageid: 'sponsors'),
                new MenuItem(title: 'Development', pageid: 'development'),
                new MenuItem(title: 'Blog', url: 'http://pveentjer.wordpress.com'),
                new MenuItem(title: 'License', pageid: 'license')
        ]),

        new Menu(name: 'Documentation', items: [
                new MenuItem(title: 'Overview', pageid: 'documentationoverview'),
                new MenuItem(title: 'Reference Manual', url: 'manual.html'),
                new MenuItem(title: 'Javadoc', url: 'apidocs/index.html')
        ])
]

//TODO: this is redundant information, all pages can be derived from the menu.
def pages = [
        new Page(pageid: '60second'),
        new Page(pageid: 'missionstatement'),
        new Page(pageid: 'development'),
        new Page(pageid: 'documentationoverview'),
        new Page(pageid: 'download'),
        new Page(pageid: 'faq'),
        new Page(pageid: 'features'),
        new Page(pageid: 'license'),
        new Page(pageid: 'overview'),
        new Page(pageid: 'sponsors')
]

def outputdirectory = "target/site"

//=============== template engine ==================

def outputdirectoryfile = new File(outputdirectory)
if (!outputdirectoryfile.exists()) {
  if (!outputdirectoryfile.mkdirs()) {
    throw new Exception("file could not be created $outputdirectory")
  }
}

for (page in pages) {
  def filename = "${page.pageid}.html"
  def engine = new GStringTemplateEngine()
  def template = engine.createTemplate(templatecontent)
  def pagecontent = new File("site/$filename").text
  def binding = [menus: menus,
          pagecontent: pagecontent,
          page: page,
          lastupdate: lastupdate]
  def result = template.make(binding).toString()
  def output = new File("$outputdirectory/$filename")
  output.parentFile.mkdirs()
  println(output.absolutePath)
  output.text = result
}

def output = new File("$outputdirectory/style.css")
output.text = new File("$basedir/site/style.css").text

def index = new File("$outputdirectory/index.html")
index.text = new File("$basedir/site/index.html").text

def manual = new File("$outputdirectory/manual.html")
manual.text = new File("$basedir/site/manual.html").text

println('finished')