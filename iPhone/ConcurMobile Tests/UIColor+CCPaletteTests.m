#import <XCTest/XCTest.h>

#import "UIColor+CCPalette.h"

@interface CCPaletteTests : XCTestCase

@end

@implementation CCPaletteTests

- (void)testCCPalette {
    //XCTAssertEqualObjects([UIColor darkBlueConcur_iOS6], [UIColor colorWithRed:0.0/255.0 green:44.0/255.0 blue:106.0/255.0 alpha:1.0f], @"Incorrect palette color");
}

- (void) testLegacyColors{
    //XCTAssertTrue(([[UIColor darkBlueConcur_iOS6] isEqual:[UIColor colorWithRed:0.0/255.0 green:44.0/255.0 blue:106.0/255.0 alpha:1.0f]]), @"getdarkBlueConcur_iOS6");
    
    //XCTAssertTrue(([[UIColor darkBlueConcur_iOS6] isEqual:[UIColor colorWithRed:0.0/255.0 green:44.0/255.0 blue:106.0/255.0 alpha:1.0f]]), @"getNavBarTintColor");
    
    XCTAssertTrue(([[UIColor navBarTintColor_iPad] isEqual:[UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1]]), @"getNavBarTintColor_iPad");
    
    XCTAssertTrue(([[UIColor baseBackgroundColor] isEqual:[UIColor colorWithRed:239.0/255.0 green:239.0/255.0 blue:239.0/255.0 alpha:1.0f]]), @"getBaseBackgroundColor");
    
    XCTAssertTrue(([[UIColor baseBackgroundColorLight] isEqual:[UIColor colorWithRed:231.0/255.0 green:232.0/255.0 blue:233.0/255.0 alpha:1.0f]]), @"getBaseBackgroundColorLight");
    
    XCTAssertTrue(([[UIColor blueSkyColor] isEqual:[UIColor colorWithRed:147.0/255.0 green:190.0/255.0 blue:242.0/255.0 alpha:1.0f]]), @"getBlueSky");
    
    XCTAssertTrue(([[UIColor cloudySkyColor] isEqual:[UIColor colorWithRed:99.0/255.0 green:138.0/255.0 blue:175.0/255.0 alpha:1.0f]]), @"getCloudySky");
    
    XCTAssertTrue(([[UIColor bookingBlueColor] isEqual:[UIColor colorWithRed:2.0/255.0 green:68.0/255.0 blue:121.0/255.0 alpha:1.0f]]), @"colorBookingBlue");
    
    XCTAssertTrue(([[UIColor bookingGrayColor] isEqual:[UIColor colorWithRed:127.0/255.0 green:127.0/255.0 blue:127.0/255.0 alpha:1.0f]]), @"getNavBarTintColor");
    
    XCTAssertTrue(([[UIColor bookingYellowColor] isEqual:[UIColor colorWithRed:237.0/255.0 green:179.0/255.0 blue:0.0/255.0 alpha:1.0f]]), @"colorBookingYellow");
    
    XCTAssertTrue(([[UIColor bookingRedColor] isEqual:[UIColor colorWithRed:197.0/255.0 green:45.0/255.0 blue:71.0/255.0 alpha:1.0f]]), @"colorBookingRed");
    
    XCTAssertTrue(([[UIColor bookingGreenColor] isEqual:[UIColor colorWithRed:111.0/255.0 green:153.0/255.0 blue:22.0/255.0 alpha:1.0f]]), @"colorBookingGreen");
    
    XCTAssertTrue(([[UIColor hyperLinkBlueColor] isEqual:[UIColor colorWithRed:48.0/255.0 green:144.0/255.0 blue:255.0/255.0 alpha:1.0f]]), @"colorHyperLinkBlue");
    
    XCTAssertTrue(([[UIColor corpSSOLabelGrayColor] isEqual:[UIColor colorWithRed:(85.0/255.0) green:(85.0/255.0) blue:(85.0/255.0) alpha:1.0]]), @"colorCorpSSOLabelGray");
    
    XCTAssertTrue(([[UIColor customFieldCellLabelColor] isEqual:[UIColor colorWithRed:(75.0/255.0) green:(75.0/255.0) blue:(75.0/255.0) alpha:1.0]]), @"colorCustomFieldCellLabel");
    
    XCTAssertTrue(([[UIColor homeToolBarBtnBorderColor] isEqual:[UIColor colorWithRed:186.0/255.0 green:187.0/255.0 blue:188.0/255.0 alpha:1.0]]), @"getHomeToolbarBtnBorderColor");
    
    XCTAssertTrue(([[UIColor tableBackgroundGrayColor] isEqual:[UIColor colorWithRed:222.0/255.0 green:228.0/255.0 blue:233.0/255.0 alpha:1.0]]), @"getTableBackgoundGray");
}

@end


