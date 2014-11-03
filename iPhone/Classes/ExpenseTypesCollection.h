//
//  ExpenseTypesCollection.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/31/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExpenseTypesData.h"

@interface ExpenseTypesCollection : NSObject
{
	// ETD = ExpenseTypeData
	
	NSMutableArray			*aKeys;				// Sorted array of ETD.parentExpKey, "MOSTCOMMON" at index 0
	NSMutableArray			*aParentNames;		// Sorted array of ETD.parentExpName, "MOSTCOMMON" at index 0
	
	NSMutableDictionary		*parentNames;		// Key: ETD.parentExpKey,  Value: ETD.parentExpName
	NSMutableDictionary		*keysForParent;		// Key: ETD.parentExpName, Value: ETD.parentExpKey
	
	NSMutableDictionary		*parents;			// Key: parentExpName, Value: NSMutableArray of ETD objects.
												// Also includes key: "Commonly Used" (localized) with value: {"BRKFT", "LUNCH", "DINNR", "TAXIX", ... }

	NSMutableDictionary		*expenseTypeByExpenseKey;		// Key: expKey,	Value: ETD object
	NSMutableDictionary		*supportsAttendeeDictionary;	// Key: expKey, Value: NSNumber with BOOL value
	
	NSString				*version;			// Version of the endpoint from which the expense types were obtained
	NSString				*polKey;			// Policy key for which expense types were obtained
}

@property (nonatomic, strong) NSMutableArray				*aKeys;
@property (nonatomic, strong) NSMutableArray				*aParentNames;

@property (nonatomic, strong) NSMutableDictionary			*parentNames;
@property (nonatomic, strong) NSMutableDictionary			*keysForParent;

@property (nonatomic, strong) NSMutableDictionary			*parents;

@property (nonatomic, strong) NSMutableDictionary			*expenseTypeByExpenseKey;
@property (nonatomic, strong) NSMutableDictionary			*supportsAttendeeDictionary;

@property (nonatomic, strong) NSString						*version;
@property (nonatomic, strong) NSString						*polKey;

+(ExpenseTypesCollection*) collectionFromData:(ExpenseTypesData*)etsData forChild:(BOOL)isForChild;

-(id) initWithExpenseTypeData:(ExpenseTypesData*)etsData forChild:(BOOL)isForChild;

@end
