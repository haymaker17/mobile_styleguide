#import <XCTest/XCTest.h>

#import "NSArray+Additions.h"

@interface NSArrayAdditonsTests : XCTestCase

@end

@implementation NSArrayAdditonsTests

- (void)testEmptyArray {
    
    [[NSArray arrayWithNotNilObjects:0] isEqualToArray:@[]];
    
    [[NSArray arrayWithNotNilObjects:1,nil] isEqualToArray:@[]];
    [[NSArray arrayWithNotNilObjects:2,nil,nil] isEqualToArray:@[]];
    [[NSArray arrayWithNotNilObjects:3,nil,nil,nil] isEqualToArray:@[]];
    [[NSArray arrayWithNotNilObjects:7,nil,nil,nil,nil,nil,nil,nil]  isEqualToArray:@[]];
    
    XCTAssertEqualObjects([NSArray arrayWithNotNilObjects:0], @[], @"should be empty array");
    
    XCTAssertEqualObjects(([NSArray arrayWithNotNilObjects:1,nil]), @[], @"should be empty array");
    XCTAssertEqualObjects(([NSArray arrayWithNotNilObjects:2,nil,nil]), @[], @"should be empty array");
    XCTAssertEqualObjects(([NSArray arrayWithNotNilObjects:3,nil,nil,nil]), @[], @"should be empty array");
    XCTAssertEqualObjects(([NSArray arrayWithNotNilObjects:7,nil,nil,nil,nil,nil,nil,nil]), @[], @"should be empty array");
    
}


@end