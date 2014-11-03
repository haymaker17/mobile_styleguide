//
//  ActiveRequestDigestMenu.h
//  ConcurMobile
//
//  Created by laurent mery on 13/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ActiveRequestDigestMenu : NSObject

@property (nonatomic, strong) UITableView *tableViewMenu;

-(id)initOnView:(UIView*)view;
-(void)show;
-(void)hide;

@end
