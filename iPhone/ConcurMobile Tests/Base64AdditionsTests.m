#import <XCTest/XCTest.h>

#import "Base64Additions.h"

@interface Base64AdditionsTests : XCTestCase

@end

@implementation Base64AdditionsTests

- (void)testBase64AdditionsOnString {
    XCTAssertTrue([NSString respondsToSelector:@selector(stringFromBase64String:)],
                  @"Should respond to stringFromBase64String");
    
    XCTAssertTrue([@"" respondsToSelector:@selector(base64String)],
                  @"Should respond to base64String");
    
    NSString *test = @"test string with some crazy thing !!@#$%%&&)(_)(_)(&%^^&)_)+";
    
    XCTAssertEqualObjects([test base64String],
                          @"dGVzdCBzdHJpbmcgd2l0aCBzb21lIGNyYXp5IHRoaW5nICEhQCMkJSUmJikoXykoXykoJiVeXiYpXykr",
                          @"Should base64 encode and decode correctly");
    
    NSArray *pngPaths = [[NSBundle mainBundle] pathsForResourcesOfType:@"png" inDirectory:nil];
    
    if ([pngPaths count] > 0) {
        NSData *tmp = [NSData dataWithContentsOfFile:[pngPaths objectAtIndex:0]];
        
        XCTAssertEqualObjects(tmp, [NSData dataWithBase64String:[tmp base64String]],
                              @"Should base64 encode and decode correctly larger objects");
    }
}

@end
