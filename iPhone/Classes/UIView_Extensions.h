//
//  UIView_Extensions.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface  UIView (extensions)
-(void)inspect:(int)level;
-(void)removeAllSubviews;
-(void)removeSubviewOfClass:(Class) subview;
@end
