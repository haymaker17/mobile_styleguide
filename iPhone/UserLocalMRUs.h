//
//  UserLocalMRUs.h
//  ConcurMobile
//
//  Created by yiwen on 8/1/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

// User MRU keys to plist file
extern NSString* const MRU_ENTRY_TRANS_DATE;
extern NSString* const MRU_ENTRY_LOC_NAME;
extern NSString* const MRU_ENTRY_MOD_DATE;
extern NSString* const MRU_ENTRY_LOC_LI_KEY;


@interface UserLocalMRUs : NSObject 
{
    NSMutableDictionary* dict;
}

@property (strong, nonatomic) NSMutableDictionary *dict;

+(UserLocalMRUs*)sharedInstance;

- (NSObject*) getMRUItem:(NSString*) key;
-(void) saveMRUItem:(NSObject*)value withKey:(NSString*)key;

@end
