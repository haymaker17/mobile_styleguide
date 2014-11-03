#import <XCTest/XCTest.h>

#import "NSString+Validations.h"
#import "FormatUtils.h"

@interface NSStringValidationsTests : XCTestCase

@end

@implementation NSStringValidationsTests

- (void)setUp
{
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown
{
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testValidEMail
{
    NSArray *validEmails = @[@"niceandsimple@example.com",
                             @"very.common@example.com",
                             @"a.little.lengthy.but.fine@dept.example.com"];
    for(NSString *anEmail in validEmails){

        XCTAssertTrue([anEmail isValidEmail], @"%@ should be a valid email", anEmail);
        XCTAssertTrue([FormatUtils isValidEmail:anEmail], @"%@ should be a valid email", anEmail);
    }
}

/*
- (void)testValidComplicatedEMail
{
    NSArray *validEmails = @[@"email.with+symbol@example.com"];

    for(NSString *anEmail in validEmails){

        XCTAssertTrue([anEmail isValidEmail], @"%@ should be a valid email", anEmail);
        XCTAssertTrue([FormatUtils isValidEmail:anEmail], @"%@ should be a valid email", anEmail);
    }
}

- (void)testValidVeryComplicatedEMail
{
    NSArray *validEmails = @[@"user@[IPv6:2001:db8:1ff::a0b:dbd0]",
                             @"\"much.more unusual\"@example.com",
                             @"\"very.unusual.@.unusual.com\"@example.com",
                             @"postbox@com",
                             @"admin@mailserver1",
                             @"!#$%&'*+-/=?^_`{}|~@example.org"];
    
    for(NSString *anEmail in validEmails){
        
        XCTAssertTrue([anEmail isValidEmail], @"%@ should be a valid email", anEmail);
        XCTAssertTrue([FormatUtils isValidEmail:anEmail], @"%@ should be a valid email", anEmail);
    }
}
*/
- (void)testNotValidEMail
{
    NSArray *notValidEmails = @[@"Abc.example.com",
                                @"A@b@c@example.com",
                                @"\"not\"right@example.com"];
    for(NSString *anEmail in notValidEmails){
        
        XCTAssertFalse([anEmail isValidEmail], @"%@ should be a valid email", anEmail);
        XCTAssertFalse([FormatUtils isValidEmail:anEmail], @"%@ should be a valid email", anEmail);
    }
}

@end
