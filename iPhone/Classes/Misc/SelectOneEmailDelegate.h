//
//  SelectOneEmailDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 12/1/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol SelectOneEmailDelegate <NSObject>
-(void) emailSelected:(NSString*) email;
-(void) contactSelectedHasNoEmail;
-(void) selectFromContacts;
@end
