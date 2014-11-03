//
//  ListFieldSearchData.h
//  ConcurMobile
//
//  Created by yiwen on 11/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ListItem.h"

@interface ListFieldSearchData : MsgResponderCommon 
{
	NSString		*fieldId;		// e.g. CrnKey, PolKey
	NSString		*ftCode;		// e.g. PRTINFO
	NSString		*isMru;			// Y/N, default to N
	NSString		*listKey;		// e.g. 1
	NSString		*parentLiKey;	//1101
	NSString		*query;			// e.g. '*' or 'Dev'
	NSString		*rptKey;		// for ReceiptType
	NSString        *searchBy;      // Empty/'TEXT' or 'CODE'
    
	NSMutableArray	*listItems;
	ListItem		*item;
	NSString		*extraFieldId;
}

@property (nonatomic, strong) NSString		*fieldId;
@property (nonatomic, strong) NSString		*ftCode;
@property (nonatomic, strong) NSString		*isMru;
@property (nonatomic, strong) NSString		*listKey;
@property (nonatomic, strong) NSString		*parentLiKey;
@property (nonatomic, strong) NSString		*query;
@property (nonatomic, strong) NSString		*rptKey;

@property (nonatomic, strong) NSMutableArray*listItems;
@property (nonatomic, strong) ListItem		*item;
@property (nonatomic, strong) NSString		*extraFieldId;
@property (nonatomic, strong) NSString      *searchBy;

- (void)encodeWithCoder:(NSCoder *)coder;
- (id)initWithCoder:(NSCoder *)coder;

-(Msg *)newMsg: (NSMutableDictionary *)parameterBag;
-(NSString *)getMsgIdKey;
-(BOOL) isFieldEmpty:(NSString*)val;

// Returns first ListItem with matching liKey
- (ListItem *)getListItemWithLiKey:(NSString *)liKey;

// Returns first ListItem with matching liCode
- (ListItem *)getListItemWithLiCode:(NSString *)liCode;

@end
