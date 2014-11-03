//
//  ExpenseTypesCollection.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/31/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExpenseTypesCollection.h"


@implementation ExpenseTypesCollection

@synthesize aKeys;
@synthesize aParentNames;
@synthesize parentNames;
@synthesize keysForParent;
@synthesize parents;
@synthesize expenseTypeByExpenseKey;
@synthesize supportsAttendeeDictionary;
@synthesize version;
@synthesize	polKey;

+(ExpenseTypesCollection*) collectionFromData:(ExpenseTypesData*)etsData forChild:(BOOL)isForChild
{
	ExpenseTypesCollection *col = nil;
	
	if (etsData != nil && etsData.ets != nil)
	{
		col = [[ExpenseTypesCollection alloc] initWithExpenseTypeData:etsData forChild:isForChild];
	}
	else
	{
		col = [[ExpenseTypesCollection alloc] init];
	}

	return col;
}

-(id) init
{
	self = [super init];
	if (self)
    {
        self.aKeys = [[NSMutableArray alloc] initWithObjects:nil];
       
        
        self.aParentNames  = [[NSMutableArray alloc] initWithObjects:nil];
        
        
        self.parentNames  = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        
        self.keysForParent  = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        
        self.parents  = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        
        self.expenseTypeByExpenseKey = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        
        self.supportsAttendeeDictionary = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
	}
	return self;
}

-(id) initWithExpenseTypeData:(ExpenseTypesData*)etsData forChild:(BOOL)isForChild
{
	if (!(self = [self init])) return nil;
    
	self.version = etsData.version;
	self.polKey = etsData.polKey;
	
	for (NSString *expKey in etsData.ets)
	{
		ExpenseTypeData *et = (etsData.ets)[expKey];
		
		// Add the expense type to the dictionary of expense types
		expenseTypeByExpenseKey[expKey] = et;
		
		// Filter out parent only expense types for child entry, and child only types for parent/reg entry
		if (([et isChildOnly] && !isForChild) || ([et isParentOnly] && isForChild))
			continue;
		
		// Record whether this expense type supports attendees
		BOOL supportsAttendees = (et.supportsAttendees != nil && [et.supportsAttendees isEqualToString:@"Y"]);
		NSNumber *supportsAttendeesAsNumber = @(supportsAttendees);
		supportsAttendeeDictionary[expKey] = supportsAttendeesAsNumber;
		
		// It is unusual, but not impossible to have an expense type without parent
		if (et.parentExpName == nil)
		{
			et.parentExpName = @"";
		}
		
		if (et.parentExpKey == nil)
		{
			et.parentExpKey = @"";
		}
		
		if (parents[et.parentExpName] == nil) 
		{//did not find it
			NSMutableArray *kids = [[NSMutableArray alloc] initWithObjects:et, nil]; //make array with an et in it
			parents[et.parentExpName] = kids; //add aray to the parent name as key
			[aKeys addObject:et.parentExpKey];
		}
		else 
		{
			NSMutableArray *kids = parents[et.parentExpName];
			[kids addObject:et];
		}
		
		parentNames[et.parentExpKey] = et.parentExpName;
		if(keysForParent[et.parentExpName] == nil)
		{
			keysForParent[et.parentExpName] = et.parentExpKey;
			[aParentNames addObject:et.parentExpName];
		}
	}
	
	for(NSString *pName in parents)
	{
		NSMutableArray *a = parents[pName];
		NSSortDescriptor *aSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"expName" ascending:YES];
		[a sortUsingDescriptors:@[aSortDescriptor]];
	}
	
	[aKeys sortUsingSelector:@selector(compare:)];
	[aParentNames sortUsingSelector:@selector(compare:)];
  
	return self;
}


@end
