//
//  OptionsSelectDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol OptionsSelectDelegate <NSObject>

-(void) optionSelected:(NSObject*)obj withIdentifier:(NSObject*) identifier;
-(void) optionSelectedAtIndex:(NSInteger)row withIdentifier:(NSObject*) identifier;

@end
