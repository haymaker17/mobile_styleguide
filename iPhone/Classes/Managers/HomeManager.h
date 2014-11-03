//
//  HomeManager.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/16/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseManager.h"
#import "EntityHome.h"

@interface HomeManager : BaseManager {
    
}

+(HomeManager*)sharedInstance;
-(id)init;
-(EntityHome *) makeNew;

-(void) clearAll;
-(NSManagedObject *) fetchHome:(NSString *)key;
@end
