//
//  TravelCustomFieldsManager.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TravelCustomFieldsManager.h"
#import "FormatUtils.h"

static TravelCustomFieldsManager *sharedInstance;

@implementation TravelCustomFieldsManager

@synthesize context = _context;
@synthesize entityName;

+(TravelCustomFieldsManager*)sharedInstance
{
	if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[TravelCustomFieldsManager alloc] init];
			}
		}
		return sharedInstance;
	}
}


-(id)init
{
    self = [super init];
	if (self)
	{
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
        self.entityName = @"EntityTravelCustomFields";
	}
    
	return self;
}



#pragma mark -
#pragma mark Expense Types default coredata
-(void) saveIt:(NSManagedObject *) obj
{
    NSError *error;
    if (![self.context save:&error])
        NSLog(@"Whoops, couldn't save object: %@", [error localizedDescription]);        
}

-(BOOL) hasAny
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return YES;
    else
        return NO;
}


-(NSArray *) fetchAll
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(NSArray *) fetchAllRequiredFieldsAtStart:(BOOL)atStart
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(required = %@ and displayAtStart = %@)", @YES, @(atStart)];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(NSArray *) fetchAllRequiredFields
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(required = %@)", @YES];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(NSArray *) fetchAllFieldsWithAttributeValue
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(attributeValue != nil)"];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(NSArray *) fetchAllFieldsAtStart:(BOOL) atStart
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(displayAtStart = %@)", @(atStart)];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
    
}

-(NSManagedObject *) makeNew
{
    return [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:self.context];
}


-(NSManagedObject *) fetchOrMake:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(attributeId = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return [self makeNew];
}

-(NSManagedObject *) fetchById:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(attributeId = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}

-(void) deleteObj:(NSManagedObject *)obj
{
    [_context deleteObject:obj];
    NSError *error;
    if (![_context save:&error]) {
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
    }
}

-(void) deleteAll
{
    NSArray *a = [self fetchAll];
    for(EntityTravelCustomFields *etcf in a)
        [self deleteObj:etcf];
}

-(NSManagedObject *) fetchOrMakeAttribute:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityTravelCustomFieldAttribute" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(valueId = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return [self makeNewAttribute];
}

-(NSManagedObject *) fetchByAttributeId:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityTravelCustomFieldAttribute" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(valueId = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}


-(EntityTravelCustomFieldAttribute *) makeNewAttribute
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityTravelCustomFieldAttribute" inManagedObjectContext:self.context];
}

-(int) getNumberOfFields
{
    NSArray *tripFields = [self fetchAll];
    
    return (tripFields != nil)?[tripFields count]:0;
}

-(int) getNumberOfAttributesForFieldId:(NSString *)attributeId
{
    EntityTravelCustomFields *tcf = (EntityTravelCustomFields *)[self fetchById:attributeId];
    
    return (tcf != nil)?[tcf.relAttribute count]:0;
}

-(NSInteger)getCustomFieldIndex:(EntityTravelCustomFields *)field forAttributeValue:(NSString *)value
{
    NSInteger index = -1;
    
    NSArray *tcfAttributes = (NSArray *)[field.relAttribute allObjects];
    
    if (tcfAttributes != nil && [tcfAttributes count] > 0) 
    {
        for (EntityTravelCustomFieldAttribute *tcfa in tcfAttributes) 
        {
            NSString *text = ([tcfa.optionText lengthIgnoreWhitespace])? tcfa.optionText : tcfa.value;
            
            if ([text isEqualToString:value])
            {
                index = [tcfa.sequence integerValue];
                break;
            }
        }
    }
    
    return index;
}

-(BOOL) hasPendingRequiredTripFieldsAtStart:(BOOL) atStart
{
    NSArray *allRequiredFields = [[TravelCustomFieldsManager sharedInstance] fetchAllRequiredFieldsAtStart:atStart];
    
    for (EntityTravelCustomFields *tcf in allRequiredFields)
    {
        if (tcf.attributeValue == nil)
            return TRUE;
    }
    return FALSE;
}

-(BOOL) hasPendingRequiredTripFields
{
    NSArray *allRequiredFields = [[TravelCustomFieldsManager sharedInstance] fetchAllRequiredFields];
    
    for (EntityTravelCustomFields *tcf in allRequiredFields)
    {
        if (tcf.attributeValue == nil)
            return TRUE;
    }
    return FALSE;
}


+(NSString *)makeCustomFieldsRequestXMLBody
{
    NSArray *customFields = [[TravelCustomFieldsManager sharedInstance] fetchAllFieldsWithAttributeValue];
    
    __autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<CustomFields>"];
    
    for (EntityTravelCustomFields *field in customFields) 
    {
        if ([field.attributeValue length])
        {
            [bodyXML appendString:@"<Field>"]; 
            [bodyXML appendString:[NSString stringWithFormat:@"<Id>%@</Id>",[FormatUtils makeXMLSafe:field.attributeId]]]; 
            [bodyXML appendString:[NSString stringWithFormat:@"<Value>%@</Value>",[FormatUtils makeXMLSafe:field.attributeValue]]];             
            [bodyXML appendString:@"</Field>"]; 
        }
    }
    
    [bodyXML appendString:@"</CustomFields>"];
    
    return (NSString *)bodyXML;
}
@end
