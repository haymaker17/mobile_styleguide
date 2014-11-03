#import <XCTest/XCTest.h>

#import "NSString+Additions.h"
#import "NSStringAdditions.h"

@interface NSStringAdditonsTests : XCTestCase

@end

@implementation NSStringAdditonsTests

- (void)testEmptyStringsInstance {
    
    XCTAssertTrue((![@"" length]), @"should handle empty string");
    XCTAssertTrue((![(NSString*)nil length]), @"should handle empty string");
    XCTAssertFalse((![@"not empty" length]), @"should handle empty string");
    XCTAssertFalse((![@" " length]), @"should handle empty string");
}

- (void)testEmptyStringsClass {
    
    XCTAssertTrue(([NSString isEmpty:@""]), @"should handle empty string");
    XCTAssertTrue(([NSString isEmpty:nil]), @"should handle empty string");
    XCTAssertFalse(([NSString isEmpty:@"not empy"]), @"should handle empty string");
    XCTAssertFalse(([NSString isEmpty:@" "]), @"should handle empty string");
}

- (void)testEmptyStringsIngoreWhitespaceInstance {
    
    XCTAssertTrue((![@"" lengthIgnoreWhitespace]), @"should handle empty string");
    XCTAssertTrue((![(NSString*)nil lengthIgnoreWhitespace]), @"should handle empty string");
    XCTAssertFalse((![@"not empty" lengthIgnoreWhitespace]), @"should handle not empty string");
    XCTAssertTrue((![@" " lengthIgnoreWhitespace]), @"should handle not empty string");
    XCTAssertTrue((![@"            " lengthIgnoreWhitespace]), @"should handle not empty string");
}

- (void)testEmptyStringsIngoreWhitespaceClass {
    
    XCTAssertTrue(([NSString isEmptyIgnoreWhitespace:@""]), @"should handle empty string");
    XCTAssertTrue(([NSString isEmptyIgnoreWhitespace:nil]), @"should handle empty string");
    XCTAssertFalse(([NSString isEmptyIgnoreWhitespace:@"not empy"]), @"should handle not empty string");
    XCTAssertTrue(([NSString isEmptyIgnoreWhitespace:@"  "]), @"should handle not empty string");
    XCTAssertTrue(([NSString isEmptyIgnoreWhitespace:@"            "]), @"should handle not empty string");
}

- (void)testXMLentities {
    
    XCTAssertTrue([[NSString stringByEncodingXmlEntities:@"\""] isEqualToString:@"&quot;"],@"\" encode" );
    XCTAssertTrue([[NSString stringByEncodingXmlEntities:@"&"] isEqualToString:@"&amp;"],@"& encode" );
    XCTAssertTrue([[NSString stringByEncodingXmlEntities:@"'"] isEqualToString:@"&apos;"],@"' encode" );
    XCTAssertTrue([[NSString stringByEncodingXmlEntities:@"<"] isEqualToString:@"&lt;"],@"< encode" );
    XCTAssertTrue([[NSString stringByEncodingXmlEntities:@">"] isEqualToString:@"&gt;"],@"> encode" );
    


}

- (void)testXMLentitiesSentence {
    BOOL test1= [[NSString stringByEncodingXmlEntities:@"this is an example of a \"XML\" <encoded> string with ' & \" & ' & < & >"] isEqualToString:@"this is an example of a &quot;XML&quot; &lt;encoded&gt; string with &apos; &amp; &quot; &amp; &apos; &amp; &lt; &amp; &gt;"];
     XCTAssertTrue(test1 ,@"should encode properly" );
}
@end
