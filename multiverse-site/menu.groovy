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

def templatecontent = new File(basedir + '/multiverse-site/site/pagetemplate.html').text

def menus = [
        new Menu(name: 'Menu', items: [
                new MenuItem(title: 'Overview', pageid: 'overview'),
                new MenuItem(title: 'Download', pageid: 'download'),
                new MenuItem(title: 'Features', pageid: 'features', items: [
                        new SubMenuItem(title: 'Release 0.7', pageid: 'release-0.7'),
                        new SubMenuItem(title: 'Release 0.6', pageid: 'release-0.6'),
                        new SubMenuItem(title: 'Release 0.5', pageid: 'release-0.5'),
                        new SubMenuItem(title: 'Release 0.4', pageid: 'release-0.4'),
                        new SubMenuItem(title: 'Release 0.3', pageid: 'release-0.3')
                ]),
                new MenuItem(title: 'Benchmarks', pageid: 'benchmarks', items: [
                        new SubMenuItem(title: 'Atomic Operations', pageid: 'benchmark-atomic'),
                        new SubMenuItem(title: 'Update', pageid: 'benchmark-update'),
                        new SubMenuItem(title: 'Read', pageid: 'benchmark-read'),
                        new SubMenuItem(title: 'Miscellaneous', pageid: 'benchmark-misc'),
                        new SubMenuItem(title: 'Test Rig', pageid: 'benchmark-testrig')
                ]),
                new MenuItem(title: 'Mission Statement', pageid: 'missionstatement'),
                new MenuItem(title: 'NoSQL', pageid: 'nosql'),
                new MenuItem(title: 'Sponsors', pageid: 'sponsors'),
                new MenuItem(title: 'Team', pageid: 'team'),
                new MenuItem(title: 'Development', pageid: 'development'),
                new MenuItem(title: 'Support', pageid: 'support'),
                new MenuItem(title: 'Blog', url: 'http://pveentjer.wordpress.com'),
                new MenuItem(title: 'License', pageid: 'license')
        ]),

        new Menu(name: 'Documentation', items: [
                new MenuItem(title: 'Overview', pageid: 'documentationoverview'),
                new MenuItem(title: 'Reference Manual', url: 'manual/index.html'),
                new MenuItem(title: 'Javadoc', url: 'javadoc/index.html')
        ])
]

//this is redundant information, all pages can be derived from the menu.
def pages = [
        new Page(pageid: '60second'),
        new Page(pageid: 'architecture'),
        new Page(pageid: 'benchmarks'),
        new Page(pageid: 'benchmark-atomic'),
        new Page(pageid: 'benchmark-misc'),
        new Page(pageid: 'benchmark-testrig'),
        new Page(pageid: 'benchmark-update'),
        new Page(pageid: 'benchmark-read'),
        new Page(pageid: 'contact'),
        new Page(pageid: 'development'),
        new Page(pageid: 'developconfiguration'),
        new Page(pageid: 'documentationoverview'),
        new Page(pageid: 'download'),
        new Page(pageid: 'faq'),
        new Page(pageid: 'features'),
        new Page(pageid: 'release-0.3'),
        new Page(pageid: 'release-0.4'),
        new Page(pageid: 'release-0.5'),
        new Page(pageid: 'release-0.6'),
        new Page(pageid: 'release-0.7'),
        new Page(pageid: 'flyingstart'),
        new Page(pageid: 'license'),
        new Page(pageid: 'missionstatement'),
        new Page(pageid: 'nosql'),
        new Page(pageid: 'otherjvmlanguages'),
        new Page(pageid: 'overview'),
        new Page(pageid: 'sponsors'),
        new Page(pageid: 'support'),
        new Page(pageid: 'team'),
        new Page(pageid: 'tutorial')
]

def outputdirectory = "multiverse-site/build/site"

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
  def pagecontent = new File("$basedir/multiverse-site/site/$filename").text
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
output.text = new File("$basedir/multiverse-site/site/style.css").text

def settingsxml = new File("$outputdirectory/settings.xml")
settingsxml.text = new File("$basedir/multiverse-site/site/settings.xml").text

def index = new File("$outputdirectory/index.html")
index.text = new File("$basedir/multiverse-site/site/index.html").text

//def pomxml = new File("$outputdirectory/pom.xml")
//pomxml.text = new File("$basedir/multiverse-site/site/pom.xml").text

println('finished')