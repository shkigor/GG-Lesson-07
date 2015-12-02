import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.DOMBuilder
import groovy.xml.dom.DOMCategory

/**
 * Created by igor on 02.12.15.
 */
def text ='''
<list>
    <technology>
        <name>Groovy</name>
    </technology>
</list>
'''

def listXmlParser = new XmlParser().parseText(text)

assert listXmlParser instanceof groovy.util.Node
assert listXmlParser.technology.name.text() == 'Groovy'

def listXmlSlurper = new XmlSlurper().parseText(text)

assert listXmlSlurper instanceof groovy.util.slurpersupport.GPathResult
assert listXmlSlurper.technology.name == 'Groovy'


def CAR_RECORDS = '''
  <records>
    <car name='HSV Maloo' make='Holden' year='2006'>
      <country>Australia</country>
      <record type='speed'>Production Pickup Truck with speed of 271kph</record>
    </car>
    <car name='P50' make='Peel' year='1962'>
      <country>Isle of Man</country>
      <record type='size'>Smallest Street-Legal Car at 99cm wide and 59 kg in weight</record>
    </car>
    <car name='Royale' make='Bugatti' year='1931'>
      <country>France</country>
      <record type='price'>Most Valuable Car at $15 million</record>
    </car>
  </records>
'''



def reader = new StringReader(CAR_RECORDS)
def doc = DOMBuilder.parse(reader)
def records = doc.documentElement

use(DOMCategory) {
    assert records.car.size() == 3
}


String books = '''
    <response version-api="2.0">
        <value>
            <books>
                <book available="20" id="1">
                    <title>Don Xijote</title>
                    <author id="1">Manuel De Cervantes</author>
                </book>
                <book available="14" id="2">
                    <title>Catcher in the Rye</title>
                   <author id="2">JD Salinger</author>
               </book>
               <book available="13" id="3">
                   <title>Alice in Wonderland</title>
                   <author id="3">Lewis Carroll</author>
               </book>
               <book available="5" id="4">
                   <title>Don Xijote</title>
                   <author id="4">Manuel De Cervantes</author>
               </book>
           </books>
       </value>
    </response>
'''



def response = new XmlSlurper().parseText(books)
def authorResult = response.value.books.book[0].author

assert authorResult.text() == 'Manuel De Cervantes'



//def response = new XmlSlurper().parseText(books)

def book = response.value.books.book[0]
def bookAuthorId1 = book.@id
def bookAuthorId2 = book['@id']

assert bookAuthorId1 == '1'
assert bookAuthorId1.toInteger() == 1
assert bookAuthorId1 == bookAuthorId2

def catcherInTheRye = response.value.books.'*'.find { node->
 /* node.@id == 2 could be expressed as node['@id'] == 2 */
    node.name() == 'book' && node.@id == '2'
}

assert catcherInTheRye.title.text() == 'Catcher in the Rye'

def bookId = response.'**'.find { book1->
    book1.author.text() == 'Lewis Carroll'
}.@id

assert bookId == 3


def titles = response.'**'.findAll{ node-> node.name() == 'title' }*.text()

assert titles.size() == 4

def titles2 = response.value.books.book.findAll{book2->
 /* You can use toInteger() over the GPathResult object */
    book2.@id.toInteger() > 2
}*.title

assert titles2.size() == 2



def jsonSlurper = new JsonSlurper()
def object = jsonSlurper.parseText('{ "name": "John Doe" } /* some comment */')

assert object instanceof Map
assert object.name == 'John Doe'

def object1 = jsonSlurper.parseText('{ "myList": [4, 8, 15, 16, 23, 42] }')

assert object1 instanceof Map
assert object1.myList instanceof List
assert object1.myList == [4, 8, 15, 16, 23, 42]



def object3 = jsonSlurper.parseText '''
    { "simple": 123,
      "fraction": 123.66,
      "exponential": 123e12
    }'''

assert object3 instanceof Map
assert object3.simple.class == Integer
assert object3.fraction.class == BigDecimal
assert object3.exponential.class == BigDecimal

def json = JsonOutput.toJson([name: 'John Doe', age: 42])

assert json == '{"name":"John Doe","age":42}'



class Person { String name }

def json1 = JsonOutput.toJson([ new Person(name: 'John'), new Person(name: 'Max') ])

assert json1 == '[{"name":"John"},{"name":"Max"}]'



