//
//  UILayoutView.h
//  iPadLayoutManager
//
//  Created by Manasee Kelkar on 11/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UIView_Extensions.h"

@interface UILayoutView : UIView
{
	float marginTop;
	float marginBottom;
	float marginLeft;
	float marginRight;
	BOOL stackVertically;
	float percentWidth;
	float percentHeight;
	
	float paddingTop;
	float paddingBottom;
	float paddingLeft;
	float paddingRight;
	
	BOOL autoFit;
	UIImageView *backgroundImage;
}

@property (nonatomic,assign) float marginTop;
@property (nonatomic,assign) float marginBottom;
@property (nonatomic,assign) float marginLeft;
@property (nonatomic,assign) float marginRight;

@property (nonatomic,assign) float paddingTop;
@property (nonatomic,assign) float paddingBottom;
@property (nonatomic,assign) float paddingLeft;
@property (nonatomic,assign) float paddingRight;

@property (nonatomic,assign) BOOL stackVertically;
@property (nonatomic,assign) float percentWidth;
@property (nonatomic,assign) float percentHeight;
@property (nonatomic,assign) BOOL autoFit;
@property (nonatomic,strong) UIImageView *backgroundImage;

-(void)relayout;
-(void)doRelayout;
-(void)distributePercentSizeChildren;
-(void)setBackgroundImg:(NSString*)img;
@end
