//
//  UIView_Inpect.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "UIView_Extensions.h"


@implementation UIView (extensions)

-(void)inspect:(int)level
{
	NSString *indent = @"";
	for (int i =0; i < level; i++) 
	{
		indent = [indent stringByAppendingString:@" "];
	}
	
	NSLog(@"%@ - tag:%d, x:%.1f, y:%.1f, ht: %.1f, wd: %.1f",indent,self.tag,self.frame.origin.x,self.frame.origin.y,self.frame.size.height,self.frame.size.width);
	
	for (UIView* child in [self subviews] ) 
	{
		[child inspect:level +1];
	}
}

-(void)removeAllSubviews
{
	for(UIView *v in self.subviews)
		[v removeFromSuperview];
}

-(void)removeSubviewOfClass:(Class) class
{
    if (self.subviews != nil)
    {
        for(UIView *view in self.subviews)
            if ([view isKindOfClass:class])
               [view removeFromSuperview];
    }
}

@end
