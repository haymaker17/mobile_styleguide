//
//  UILayoutView.m
//  iPadLayoutManager
//
//  Created by Manasee Kelkar on 11/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "UILayoutView.h"

@interface UILayoutView ()
-(void)distributePercentSizeChildrenVertical;
-(void)distributePercentSizeChildrenHorizontal;
@end


@implementation UILayoutView 
@synthesize marginTop, marginBottom,marginLeft,marginRight,stackVertically,percentHeight,percentWidth,autoFit;
@synthesize paddingTop, paddingBottom,paddingLeft,paddingRight,backgroundImage;


-(void)awakeFromNib
{
	[super awakeFromNib];
	//NSLog(@"INIT: %p",self);
	self.percentHeight = 0.0;
	self.percentWidth = 0.0;
	self.autoFit = YES;
	self.stackVertically = YES;
}

-(void)setBackgroundImg:(NSString*)img
{
	if (self.backgroundImage != nil) 
	{
		[backgroundImage removeFromSuperview];
	}
	self.backgroundImage = [[UIImageView alloc] init];
	[self addSubview:backgroundImage];
	self.backgroundImage.image = [UIImage imageNamed:img];
	[self sendSubviewToBack:backgroundImage];
}

-(void)relayout
{
	// 3 pass Layout Manager
	[self doRelayout];
	[self doRelayout];
	[self doRelayout];
}

-(void)doRelayout
{
	[self.backgroundImage setHidden:YES];
	[self distributePercentSizeChildren];
	
	float w = self.frame.size.width - self.paddingLeft - self.paddingRight;
	float top = self.paddingTop;
	float h = self.frame.size.height - self.paddingTop - self.paddingBottom;
	float left = self.paddingLeft;
	
	for (UILayoutView* child in [self subviews]) 
	{
		if (![child isHidden]) 
		{
			float mLeft = 0.0, mRight = 0.0, mTop = 0.0, mBottom = 0.0;
			
			if ([child isKindOfClass:[UILayoutView class]]) 
			{
				[child doRelayout];
				mLeft = child.marginLeft;
				mRight = child.marginRight;
				mTop = child.marginTop;
				mBottom = child.marginBottom;
                //NSLog(@"child.tag = %d", child.tag);
			}
            
			if (stackVertically) 
			{
				top += mTop;
                if(child.tag == 1214)
                {
                    //NSLog(@"stackVertically----------------- child:%d, w: %.1f top:%.2f", child.tag, child.frame.size.width, top);
                    top = 416;
                }
				child.frame = CGRectMake(mLeft + self.paddingLeft, top, w - mLeft - mRight, child.frame.size.height);
				top += child.frame.size.height+ mBottom; 

			}
			else 
			{
				left += mLeft;
				child.frame = CGRectMake(left, mTop + self.paddingTop, child.frame.size.width, h-mBottom-mTop);
				left +=  child.frame.size.width + mRight;
				//NSLog(@"----------------- child:%d, w: %.1f",child.tag,child.frame.size.width);
			}
		}
	}
	
	//[self distributePercentSizeChildren];
	[self.backgroundImage setHidden:NO];
	[self sendSubviewToBack:backgroundImage];
	self.backgroundImage.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
}

-(void)distributePercentSizeChildren
{
	[self.backgroundImage setHidden:YES];
	if (stackVertically) 
	{
		[self distributePercentSizeChildrenVertical];
	}
	else 
	{
		[self distributePercentSizeChildrenHorizontal];	
	}
}

-(void)distributePercentSizeChildrenVertical
{
	float totalHeight = 0, totalPercentHeight = 0;
	for (UILayoutView* child in [self subviews]) 
	{
		if ([child isHidden])
		{
			continue;
		}
		if ([child isKindOfClass:[UILayoutView class]] ) 
		{
			[child distributePercentSizeChildren];
			if (child.percentHeight > 0.1) 
			{
				totalPercentHeight += child.percentHeight;
			}
			else 
			{
				totalHeight += child.frame.size.height;
			}
			
			totalHeight += child.marginTop + child.marginBottom;
		}
		else 
		{
			totalHeight += child.frame.size.height;
		}
	}
	
	//NSLog(@"totalHeight: %.1f, total % ht: %.1f",totalHeight,totalPercentHeight);
	float usableHeight = self.frame.size.height - self.paddingTop - self.paddingBottom;
	
	for (UILayoutView* child in [self subviews]) 
	{
		if ([child isKindOfClass:[UILayoutView class]] && (child.percentHeight > 0.1) && ![child isHidden]) 
		{
			float height = (usableHeight - totalHeight)* (child.percentHeight/totalPercentHeight);
			if (height > 0.1)
				child.frame = CGRectMake(child.frame.origin.x, child.frame.origin.y, child.frame.size.width, height);
		}
	}
	
	if (totalPercentHeight <= 0.1 && autoFit && totalHeight > 0.1) 
	{
		self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.frame.size.width, totalHeight + self.paddingTop + self.paddingBottom);
	}
}

-(void)distributePercentSizeChildrenHorizontal
{
	float totalWidth = 0, totalPercentWidth = 0;
	for (UILayoutView* child in [self subviews]) 
	{
		if ([child isHidden])
		{
			continue;
		}
		
		if ([child isKindOfClass:[UILayoutView class]] ) 
		{
			if (child.percentWidth > 0.1) 
			{
				totalPercentWidth += child.percentWidth;
			}
			else 
			{
				totalWidth += child.frame.size.width;
			}
			
			totalWidth += child.marginLeft + child.marginRight;
		}
		else 
		{
			totalWidth += child.frame.size.width;
		}
	}
	
//	NSLog(@"========== tag: %d, totalWidth: %.1f, total  Percent_ht: %.1f",self.tag, totalWidth,totalPercentWidth);
	
	float usableWidth = self.frame.size.width - self.paddingLeft - self.paddingRight;
	
	for (UILayoutView* child in [self subviews]) 
	{
		if ([child isKindOfClass:[UILayoutView class]] && (child.percentWidth > 0.1) && ![child isHidden]) 
		{
			float width = (usableWidth - totalWidth)* (child.percentWidth/totalPercentWidth);
			if (width > 0.1)
			{
				//NSLog(@"------------ setting child width of %d to %.1f", child.tag,width);
				child.frame = CGRectMake(child.frame.origin.x, child.frame.origin.y, width, child.frame.size.height);
			}
		}
	}
	
	if (totalPercentWidth <= 0.1 && autoFit && totalWidth > 0.1) 
	{
		//NSLog(@"------------ setting width of %d to %.1f", self.tag,totalWidth + self.paddingLeft +self.paddingRight);
		self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, totalWidth + self.paddingLeft +self.paddingRight, self.frame.size.height);
	}
}


@end
