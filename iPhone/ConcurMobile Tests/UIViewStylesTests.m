//
//  UIViewStylesTests.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 11/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "UIColor+ConcurColor.h"
#import "UIView+Styles.h"

@interface UIViewStylesTests : XCTestCase

@end

@implementation UIViewStylesTests

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

- (void)testCornerRadius
{
    UIView *testView = [[UIView alloc] initWithFrame:CGRectMake(10, 10, 30, 30)];
    [testView setCornerRadius:5.0];
    XCTAssertTrue((testView.cornerRadius == 5.0), @"should set cornerRadius correctly");
}


- (void)testSetBordersWithColorandBorderWidth{
	
	UIView *testView = [[UIView alloc] initWithFrame:CGRectMake(10, 10, 30, 30)];
	[testView setBorders:@"l" WithColor:[UIColor blackColor] andBorderWidth:1.0];
	
	XCTAssertTrue((testView.layer.sublayers.count == 1), @"should add sublayer correctly");
	CALayer *layer = [testView.layer.sublayers objectAtIndex:0];
	XCTAssertTrue((layer.borderWidth == 1.0), @"should set borderWidth correctly");
	XCTAssertTrue((layer.borderColor == [UIColor blackColor].CGColor), @"should set borderColor correctly");
	XCTAssertTrue((testView.clipsToBounds == YES), @"should set clipsToBounds correctly");
}

- (void)testApplyStyleButtonWorkflow{
	
	UIButton *testButtonWorkflow = [[UIButton alloc]initWithFrame:CGRectMake(10, 10, 30, 30)];
	[testButtonWorkflow applyStyleButtonWorkflow];

	XCTAssertTrue([testButtonWorkflow.backgroundColor isEqual:[UIColor backgroundForMainButtonWorkflow]], @"should set background correctly");
	XCTAssertTrue([testButtonWorkflow.tintColor isEqual:[UIColor whiteConcur]], @"should set tint color correctly");
	XCTAssertTrue([testButtonWorkflow.borderColor isEqual:[UIColor borderForMainButtonWorkflow]], @"should set border color correctly");
	XCTAssertTrue(testButtonWorkflow.borderWidth == 2, @"should set border width correctly");
	XCTAssertTrue(testButtonWorkflow.cornerRadius == 3.0, @"should set corner radius correctly");
}

- (void)testApplyStyleForALabelOverButtonWorkflow{
	
	UILabel *testLabel = [[UILabel alloc]initWithFrame:CGRectMake(10, 10, 30, 30)];
	[testLabel applyStyleForALabelOverButtonWorkflow];

	XCTAssertTrue([testLabel.backgroundColor isEqual:[UIColor backgroundToHighlightTextOverMainButtonWorkflow]], @"should set background correctly");
	XCTAssertTrue([testLabel.borderColor isEqual:[UIColor borderToHighlightTextOverMainButtonWorkflow]], @"should set border color correctly");
	XCTAssertTrue([testLabel.textColor isEqual:[UIColor whiteConcur]], @"should set text color correctly");
	XCTAssertTrue(testLabel.borderWidth == 1, @"should set border width correctly");
	XCTAssertTrue(testLabel.cornerRadius == 3.0, @"should set corner radius correctly");
}

- (void)testApplyStyleWhiteBlocWithBorderTemplateOrNil{
	
	UIView *testView = [[UIView alloc]initWithFrame:CGRectMake(10, 10, 30, 30)];
	[testView applyStyleWhiteBlocWithBorderTemplateOrNil:nil];

	XCTAssertTrue([testView.backgroundColor isEqual:[UIColor whiteConcur]], @"should set background correctly");
	XCTAssertTrue([testView.tintColor isEqual:[UIColor textLightTitle]], @"should set tint color correctly");
}

- (void)testApplyFitContentByConstrainte{
	
	UILabel *testLabel = [[UILabel alloc]initWithFrame:CGRectMake(10, 10, 30, 30)];
	[testLabel setText:@"test test test test"];
	NSLayoutConstraint *constrainte = [[NSLayoutConstraint alloc]init];
	[testLabel applyFitContentByConstrainte:constrainte withMaxWidth:120 andMarge:10];
	
	XCTAssertTrue(constrainte.constant == 120.0, @"should set width correctly");
}

- (void)testApplyStyleForDisplayApprovalStatusBorderedByConstrainte{
	
	UILabel *testLabel = [[UILabel alloc]initWithFrame:CGRectMake(10, 10, 30, 30)];
	[testLabel applyStyleForDisplayApprovalStatusBorderedByConstrainte:[[NSLayoutConstraint alloc]init] withMaxWidth:120];
	
	XCTAssertTrue([testLabel.borderColor isEqual:[UIColor borderWorkflowStatutInList]], @"should set border color correctly");
	XCTAssertTrue(testLabel.borderWidth == 0.5, @"should set border width correctly");
	XCTAssertTrue(testLabel.cornerRadius == 3.0, @"should set corner radius correctly");
}
@end
